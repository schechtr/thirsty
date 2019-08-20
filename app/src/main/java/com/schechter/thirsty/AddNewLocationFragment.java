package com.schechter.thirsty;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.honorato.multistatetogglebutton.MultiStateToggleButton;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;
import static android.os.Environment.getExternalStoragePublicDirectory;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddNewLocationFragment extends Fragment {

    private static final String TAG = "AddNewLocationFragment";
    static final int REQUEST_IMAGE_CAPTURE = 101;
    private FirebaseStorage storage;
    private String currentPhotoPath;
    private Uri currentPhotoURI;
    private String currentphotoStorageURL;
    private IMainActivity iMainActivity;
    private LatLng incomingLatLng;
    private MultiStateToggleButton multiStateToggleButton;
    private Button btn_confirm_add;
    private ImageButton btn_add_a_photo;
    private MapFragment mapFragment;

    /* TODO: when user is not authenticated there should be something letting them know that they must sign in first */



    public AddNewLocationFragment(MapFragment mapFragment) {
        // Required empty public constructor
        this.mapFragment = mapFragment;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        iMainActivity = (MainActivity) getActivity();

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            final double latitude = bundle.getDouble(getString(R.string.latitude_key));
            final double longitude = bundle.getDouble(getString(R.string.longitude_key));

            Log.d(TAG, "latitude received :" + latitude);
            Log.d(TAG, "longitude received :" + longitude);

            incomingLatLng = new LatLng(latitude, longitude);

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_new_location, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        // initialize elements
        multiStateToggleButton = getView().findViewById(R.id.mstb_multi_id);
        multiStateToggleButton.enableMultipleChoice(true);


        btn_add_a_photo = getView().findViewById(R.id.btn_add_a_photo);
        btn_add_a_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

                    // take a picture
                    dispatchTakePictureIntent();

                } else {
                    checkCameraPermission();

                }
            }
        });


        btn_confirm_add = getView().findViewById(R.id.btn_confirm_add);
        btn_confirm_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final LatLng currentLocation = incomingLatLng;
                final boolean[] options = multiStateToggleButton.getStates();

                addLocationToDatabase(currentLocation, options);
            }
        });

    }

    private void addLocationToDatabase(LatLng location, boolean[] options) {

        final boolean bottle_refill = options[0];
        final boolean dog_bowl = options[1];

        final Location newLocation = new Location(getContext(), location);
        newLocation.setBottle_refill(bottle_refill);
        newLocation.setDog_bowl(dog_bowl);

        Log.d(TAG, "addLocationToDatabase: " + currentphotoStorageURL);

        if (currentphotoStorageURL != null)
            newLocation.setPhoto_url(currentphotoStorageURL);

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Locations");
        final String location_id = databaseReference.push().getKey();
        newLocation.setLocation_id(location_id);
        newLocation.findNearestPlace();

        databaseReference.child(location_id).setValue(newLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "onComplete: database upload success");


                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        final String uid = user.getUid();
                        updateFirebaseUserData(location_id, uid);
                        
                    }

                    mapFragment.showFABs();
                    //getFragmentManager().popBackStack();


                } else {
                    Log.d(TAG, "onComplete: database upload failure");
                    Toast.makeText(getContext(), "Upload Failed", Toast.LENGTH_SHORT).show();

                }
            }
        });


    }

    private void updateFirebaseUserData(final String markerID, final String uid) {

        /* update starred status */
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Users").child(uid).child("contributed");

        reference.child(markerID).setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mapFragment.showFABs();
                    getFragmentManager().popBackStack();
                } else
                    Log.d(TAG, "onComplete: error finalizing user data update");
            }
        });

    }


    /******************************************
     ******* Take and Upload a Photo **********
     ******************************************/

    private void dispatchTakePictureIntent() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {

            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // error creating file
                Log.d(TAG, "dispatchTakePictureIntent: error creating file");
                return;
            }

            if (photoFile != null) {
                currentPhotoURI = FileProvider.getUriForFile(getContext(),
                        "com.schechter.thirsty.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE: {
                if (resultCode == RESULT_OK) {
                    Log.d(TAG, "onActivityResult: received picture");
                    uploadPictureToFirebase();
                }
            }
        }


    }

    private File createImageFile() throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, getString(R.string.image_extension), storageDir);

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void uploadPictureToFirebase() {

        currentphotoStorageURL = "images/" + System.currentTimeMillis() + getString(R.string.image_extension);
        StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child(currentphotoStorageURL);


        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Uploading Picture...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setProgress(0);
        progressDialog.show();

        storageReference.putFile(currentPhotoURI)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // upload success
                        Log.d(TAG, "onSuccess: image successfully uploaded");
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Successfully Uploaded", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // upload failure
                        Log.d(TAG, "onFailure: image upload failed");
                        currentphotoStorageURL = "";
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Upload Failed", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        // can show information about progress (maybe for a progress bar)
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                .getTotalByteCount());
                        Log.d(TAG, "onProgress: " + progress + "% uploaded");
                        //progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        progressDialog.setProgress((int) progress);
                    }
                });
    }

    /*** handle camera permission */
    private void checkCameraPermission() {

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        dispatchTakePictureIntent();
                    }
                } else {
                    Toast.makeText(getContext(), "please allow location permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

}
