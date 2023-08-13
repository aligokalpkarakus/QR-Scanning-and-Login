package com.aligokalpkarakus.qrandlogin;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;

public class QRActivity extends AppCompatActivity {

    private MaterialButton cameraButton;
    private MaterialButton galleryButton;
    private MaterialButton scanButton;
    private ImageView imageView;
    private TextView textView;


    //onRequestPermissionResults için kullanacağımız sabitler
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQEUST_CODE = 101;

    private String[] cameraPermissions;
    private String[] storagePermissions;

    private Uri imageUri = null; //gelen image'ın Uri'ı

    private static final String TAG = "MAIN_TAG";

    private BarcodeScannerOptions barcodeScannerOptions;
    private BarcodeScanner barcodeScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);

        //Kullanıcı arayüzü
        cameraButton = findViewById(R.id.cameraButton);
        galleryButton = findViewById(R.id.galleryButton);
        scanButton = findViewById(R.id.scanButton);
        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);

        //izinlerin listeleri
        cameraPermissions = new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //dahil edilen kütüphanenin scanner objesini kullanmak için
        barcodeScannerOptions = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .build();
        barcodeScanner = BarcodeScanning.getClient(barcodeScannerOptions);

        //Kamera butonu ile izinleri kontrol edip kameradan barkod-QR alacağız
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // izin verildi, kamera kullanılabilir
                if(checkCameraPermission()){
                    pickImageCamera();
                }else{ //izin verilmediyse tekrar izin kontrolü
                    requestCameraPermission();
                }
            }
        });

        //Galeri butonu ile izinleri kontrol edip galeriden barkod-QR alacağız
        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //izin verildi galeri açılabilir
                if(checkStoragePermission()){
                    pickImageGallery();
                }else{ //izin verilmedi tekrar izin kontrolü
                    requestStoragePermission();
                }
            }
        });

        //Scan butonu ile galeriden veya kameradan alınan barkod-QR taranacak
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //daha image seçilmediyse
                if(imageUri == null){
                    Toast.makeText(QRActivity.this, "Bir QR kod seçiniz...", Toast.LENGTH_SHORT).show();
                }else{ //image seçildiyse tarama başlatılsın
                    detectResultFromImage();
                }
            }
        });
    }

    private void detectResultFromImage() {
        try{
            //image uri'den image hazırlama
            InputImage inputImage = InputImage.fromFilePath(this,imageUri);
            //image'dan data taraması başlatılsın
            Task<List<Barcode>> barcodeResult = barcodeScanner.process(inputImage)
                    .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                        @Override
                        public void onSuccess(List<Barcode> barcodes) {
                            //Tarama başarılı olursa çalışacak ve QRdan alınan bilgiler verilecek
                            extractBarcodeQRCodeInfo(barcodes);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //Tarama başarısız olursa çalışacak bilgiler alınamayacak
                            Toast.makeText(QRActivity.this, "Tarama hatası: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }catch (Exception e){
            Toast.makeText(this, "Hata: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void extractBarcodeQRCodeInfo(List<Barcode> barcodes) {
        //barkodlardan bilgi alama
        for(Barcode barcode : barcodes){
            Rect bounds = barcode.getBoundingBox();
            Point[] corners = barcode.getCornerPoints();

            //raw info barkod-QRdan tarandı
            String rawValue = barcode.getRawValue();
            Log.d(TAG, "extractBarcodeQRCodeInfo: rawValue: " + rawValue);

            int valueType = barcode.getValueType();

            switch (valueType) {
                //wifi related data için
                case Barcode.TYPE_WIFI: {
                    Barcode.WiFi typeWifi = barcode.getWifi();

                    String ssid = "" + typeWifi.getSsid();
                    String password = "" + typeWifi.getPassword();
                    String type = "" + typeWifi.getEncryptionType();

                    //textView'e bağlama
                    textView.setText("TYPE: TYPE_WIFI \nssid: " + ssid + "\npassword: " + password + "\nencryptionType: " + type + "\nraw value: " + rawValue);
                }
                break;

                //url related data
                case Barcode.TYPE_URL: {
                    Barcode.UrlBookmark typeUrl = barcode.getUrl();

                    String title = typeUrl.getTitle();
                    String url = typeUrl.getUrl();


                    textView.setText("TYPE: TYPE_URL \ntitle: " + title + "\nurl: " + url + "\nraw value: " + rawValue);
                }
                break;

                //email related data
                case Barcode.TYPE_EMAIL:{
                    Barcode.Email typeEmail = barcode.getEmail();

                    String address = ""+ typeEmail.getAddress();
                    String body = ""+ typeEmail.getBody();
                    String subject = ""+ typeEmail.getSubject();

                    textView.setText("TYPE: TYPE_EMAIL \naddress: " + address + "\nbody: " + body + "\nsubject: " + subject + "\nraw value: " + rawValue);
                }
                break;

                case Barcode.TYPE_CONTACT_INFO:{
                    Barcode.ContactInfo typeContact = barcode.getContactInfo();

                    String title = "" + typeContact.getTitle();
                    String organizer = "" + typeContact.getOrganization();
                    String name = "" + typeContact.getName().getFirst() + " " + typeContact.getName().getLast();
                    String phones = "" + typeContact.getPhones().get(0).getNumber();

                    textView.setText("TYPE: TYPE_CONTACT_INFO \ntitle: " + title + "\norganizer: " + organizer + "\nname: " + name + "\nphones: "+ phones + "\nraw value: " + rawValue);

                }
                break;

                default:{
                    textView.setText("raw value: " + rawValue);
                }

            }
        }
    }

    private void pickImageGallery(){
        //Intent ile galeriden image seçmemiz sağlanıyor
        Intent intent = new Intent(Intent.ACTION_PICK);
        //Seçmek istediğimiz dosya türü
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    //image'ı galeriden çekip sergilediğimiz kısım
                    if(result.getResultCode() == Activity.RESULT_OK){
                        //image seçildikten sonra seçtiğimiz image'ın uri'ını alıyoruz
                        Intent data = result.getData();
                        imageUri = data.getData();
                        Log.d(TAG,"onActivityResult: imageUri: "+ imageUri);
                        //imageView ile seçtiğimiz image'ı bağlıyoruz
                        imageView.setImageURI(imageUri);
                    }else{
                        //galeriye giriş izni reddedilirse çıkacak mesaj
                        Toast.makeText(QRActivity.this,"Permission Denied",Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private void pickImageCamera(){
        //MediaStore'da image data'yı depolamak için hazırlanış
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"Sample Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Sample Image Description");
        //Image Uri
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
        //Kamerayı başlatmak için launch
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncer.launch(intent);
    }

    private final ActivityResultLauncher<Intent> cameraActivityResultLauncer = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    //kameradan QR ve barkodu aldığımız yer
                    if(result.getResultCode() == Activity.RESULT_OK){
                        //kameradan alınan image
                        Intent data = result.getData();
                        //imageUriyi tekrar kaydetmedik pickImageCamera()'da uriyi çekmiştik
                        Log.d(TAG, "onActivityResult: imageUri: "+imageUri);
                        imageView.setImageURI(imageUri);
                    }else{
                        //kameraya giriş izni reddedilirse çıkacak mesaj
                        Toast.makeText(QRActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private boolean checkStoragePermission(){
        //depolama için izin verildi mi kontrolü verildiyse true verilmediyse false
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
        return result;
    }

    private void requestStoragePermission(){
        //galeriden image almak için storage permission
        ActivityCompat.requestPermissions(this,storagePermissions,STORAGE_REQEUST_CODE);
    }

    private boolean checkCameraPermission(){
        //kamera için hem kameranın kendisini hem de storage izni gerekiyor o yüzden WRITE_EXTERNAL_STORAGE yine var
        //kamera izni kontrolü
        boolean resultCamera = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
        //depolama izni kontrolü
        boolean resultStorage = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;

        return resultCamera && resultStorage;
    }

    private void requestCameraPermission(){
        //kamera için izin istenmesi
        ActivityCompat.requestPermissions(this,cameraPermissions,CAMERA_REQUEST_CODE);
    }

    //runtime izin sonuçları
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode){
            case CAMERA_REQUEST_CODE:{
                if(grantResults.length > 0){
                    //kamera ve storage izinleri verildi mi
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    //ikisi de verildiyse kamera başlatılacak
                    if(cameraAccepted && storageAccepted){
                        pickImageCamera();
                    }else{ // en az birine izin verilmediyse kamera başlatılmayacak
                        Toast.makeText(this, "Kamera ve Depolama İçin İzin Gerekmektedir...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQEUST_CODE:{
                if(grantResults.length > 0){
                    // storage için izin verildi mi
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    //verildiyse galeriyi aç verilmediyse açma
                    if(storageAccepted){
                        pickImageGallery();
                    }else{
                        Toast.makeText(this, "Galeriye Girmek İçin Storage İzni Gerekmektedir...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
    }


}