package com.example.uploaddata;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class UploadData extends AppCompatActivity {

    // Folder path for Firebase Storage.
    String Storage_Path = "photos/";

    // Root Database Name for Firebase Database.
    String Database_Path = "All_Image_Uploads_Database";

    Button ChooseButton, UploadButton;
    EditText ImageName;
    Spinner animalType;
    ImageView SelectImage;

    // Creating URI.
    Uri FilePathUri,croppedUri;


    // Creating StorageReference and DatabaseReference object.
    StorageReference storageReference;
    DatabaseReference databaseReference;

    // Image request code for onActivityResult() .
    int Image_Request_Code = 7;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_data);
        // Assign FirebaseStorage instance to storageReference.
        storageReference = FirebaseStorage.getInstance().getReference();

        // Assign FirebaseDatabase instance with root database name.
        databaseReference = FirebaseDatabase.getInstance().getReference(Database_Path);

        ChooseButton = (Button) findViewById(R.id.chooseImage);
        UploadButton = (Button) findViewById(R.id.uploadbt);
        ImageName = (EditText) findViewById(R.id.imageName);
        SelectImage = (ImageView) findViewById(R.id.uploadedImage);
        animalType = findViewById(R.id.animalType);

        progressDialog = new ProgressDialog(UploadData.this);

        ChooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSelectImageClick(view);
//                Intent intent = new Intent();
//                intent.setType("image/*");
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                startActivityForResult(Intent.createChooser(intent, "Please Select Image"), Image_Request_Code);

            }
        });

        UploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UploadImageFileToFirebaseStorage();
            }
        });
    }
    public void onSelectImageClick(View view) {
        CropImage.startPickImageActivity(this);
    }

    @Override
    @SuppressLint("NewApi")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // handle result of pick image chooser
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(this, data);

            // For API >= 23 we need to check specifically that we have permissions to read external storage.
            if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                // request permissions and handle the result in onRequestPermissionsResult()
                croppedUri = imageUri;
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            } else {
                // no permissions required or already grunted, can start crop image activity
                startCropImageActivity(imageUri);
            }
        }

        // handle result of CropImageActivity
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Bitmap bitmap = null;
                try {
                    FilePathUri=result.getUri();
                    bitmap = compressImage(MediaStore.Images.Media.getBitmap(getContentResolver(), FilePathUri));
                    System.out.println("-------------------BEFORE "+bitmap);
                    bitmap=ImageResizer.reduceBitmapSize(bitmap,360000);
                    System.out.println("-------------------BEFORE "+bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
//
//                // Setting up bitmap selected image into ImageView.
                SelectImage.setImageBitmap(bitmap);
//
//                // After selecting image change choose button above text.
                ChooseButton.setText("Image Selected");
                Toast.makeText(this, "Cropping successful, Sample: " + result.getSampleSize(), Toast.LENGTH_LONG).show();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "Cropping failed: " + result.getError(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (croppedUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // required permissions granted, start crop image activity
            startCropImageActivity(croppedUri);
        } else {
            Toast.makeText(this, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show();
        }
    }

    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMultiTouchEnabled(true)
                .start(this);
    }

    public Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 20, baos);// 100baos
        int options = 100;
        while (baos.toByteArray().length / 1024 > 300) { // 300kb,
            baos.reset();// baosbaos
            System.out.println("#########$$$$$$$$$$$$$$###"+baos.toByteArray().length+"  "+baos.toByteArray().length / 1024);
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);// options%baos
            options -= 10;// 10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(
                baos.toByteArray());// baosByteArrayInputStream
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// ByteArrayInputStream
        return bitmap;
    }


//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == Image_Request_Code && resultCode == RESULT_OK && data != null && data.getData() != null) {
//
//            FilePathUri = data.getData();
//
//            try {
//
//                // Getting selected image into Bitmap.
//                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), FilePathUri);
//
//                // Setting up bitmap selected image into ImageView.
//                SelectImage.setImageBitmap(bitmap);
//
//                // After selecting image change choose button above text.
//                ChooseButton.setText("Image Selected");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    // Creating Method to get the selected image file Extension from File Path URI.
    public String GetFileExtension(Uri uri) {

        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        // Returning the file Extension.
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));

    }

    // Creating UploadImageFileToFirebaseStorage method to upload image on storage.
    public void UploadImageFileToFirebaseStorage() {

        // Checking whether FilePathUri Is empty or not.
        if (FilePathUri != null) {
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            // Creating second StorageReference.
            StorageReference storageReference2nd = storageReference.child(Storage_Path + System.currentTimeMillis() + "." + GetFileExtension(FilePathUri));

            // Adding addOnSuccessListener to second StorageReference.
            storageReference2nd.putFile(FilePathUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            // Getting image name from EditText and store into string variable.
                            final String TempImageName = ImageName.getText().toString().trim();
                            final String AnimalType = animalType.getSelectedItem().toString();

                            progressDialog.dismiss();

                            Toast.makeText(getApplicationContext(), "Uploaded Successfully ", Toast.LENGTH_LONG).show();

                            if (taskSnapshot.getMetadata() != null) {
                                if (taskSnapshot.getMetadata().getReference() != null) {
                                    Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            String imageUrl = uri.toString();

                                            @SuppressWarnings("VisibleForTests")
                                            ImageUploadInfo imageUploadInfo = new ImageUploadInfo(TempImageName, AnimalType, imageUrl);

                                            // Getting image upload ID.
                                            String ImageUploadId = databaseReference.push().getKey();

                                            // Adding image upload id s child element into databaseReference.
                                            databaseReference.child(ImageUploadId).setValue(imageUploadInfo);
                                            //createNewPost(imageUrl);
                                        }
                                    });
                                }
                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(UploadData.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.setTitle("Uploading...");
                        }
                    });
        } else {
            Toast.makeText(UploadData.this, "Please Select Image or Add Image Name", Toast.LENGTH_LONG).show();
        }
    }

}