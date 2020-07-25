package com.test.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Detail_history_check_modi extends AppCompatActivity {
    TextView date, ip, ist, iet, iwt;
    EditText diary;
    ImageView ivImage;
    int input_year, input_month, input_day_of_month;
    int mHour_st, mMinute_st, mHour_et, mMinute_et;
    int hour_pay;
    AlertDialog.Builder ad; // 시급변경을 할때 띄워주는 창과 관련된 선언부
    String day_of_week=null;

    Day_Info day; // 인텐트로 전달받은 데이터를 담을 객체변수
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234; // ?
    //ByteArrayOutputStream stream = new ByteArrayOutputStream();

    private final int CAMERA_CODE = 1111;
    private final int GALLERY_CODE=1112;
    private Uri photoUri;
    private String currentPhotoPath;
    String SendingPhotoPath;
    Bitmap sending_bitmap = null;



    SharedPreferences nPrefs;
    SharedPreferences.Editor Editor;
    private Gson gson;

    String Place_id;
    String Day_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_history_check_modi);
        setTitle("근무 상세 기록 및 수정");

        date = (TextView)findViewById(R.id.modi_date); // 근무 날짜
        ip = (TextView)findViewById(R.id.modi_pay); // 근무 시급
        ist = (TextView)findViewById(R.id.modi_start_time); // 근무 시작시간
        iet = (TextView)findViewById(R.id.modi_end_time); // 근무 종료 시간
        iwt = (TextView)findViewById(R.id.modi_working_time); // 근무 시간
        diary = (EditText)findViewById(R.id.dinput_diary);
        ivImage = (ImageView) findViewById(R.id.dimage_view);

        //SharedPreference 관련 코드
        gson = new Gson(); // 리스트를 통쨰로 저장하기 위한 gson 객체를 생성해준다.

        Place_id = getIntent().getStringExtra("Place_id");
        nPrefs = getSharedPreferences(Place_id, MODE_PRIVATE);
        Editor  = nPrefs.edit();
        Day_id = getIntent().getStringExtra("day_info_id");
        Log.i(getClass().toString(), "받아온 아이템 아이디값 : " + Place_id);

        day = get_day_info(Day_id);



        init();
        getCameraPermission();

    }

    private Day_Info get_day_info(String day_id) {
        String json = nPrefs.getString(day_id, null);
        return gson.fromJson(json, Day_Info.class);
    }

    private void init(){
        //날짜 부분 초기화
        input_year = day.year;
        input_month = day.month;
        input_day_of_month = day.day_of_month;
        day_of_week = day.day_of_week;
        date.setText(String.format("%d년 %02d월 %02d일 ", input_year, input_month, input_day_of_month) + day_of_week);

        // 그 외의 부분 초기화

        hour_pay = day.hour_pay; // 해당 날짜의 시급
        mHour_st = day.Day_start_time_hour; // 해당 날짜의 시작 시간
        mMinute_st = day.Day_start_time_min; // 해당 날짜의 시작 분
        mHour_et = day.Day_end_time_hour; // 해당 날짜의 끝나는 시간
        mMinute_et = day.Day_end_time_min; // 해당 날짜의 끝나는 분

        ip.setText(hour_pay + "원");
        ist.setText(String.format("%02d:%02d", mHour_st, mMinute_st));
        iet.setText(String.format("%02d:%02d", mHour_et, mMinute_et));
        iwt.setText(day.Daily_total_time_min/60 + "시간 " + day.Daily_total_time_min%60 + "분");

        if(day.daily_image_path == null){

        }else {
            File file = new File(day.daily_image_path);
            ivImage.setImageURI(Uri.fromFile(file));
        }

        /*if(day.daily_image != null){
            Bitmap bmp = BitmapFactory.decodeByteArray(day.daily_image, 0, day.daily_image.length); // 인텐트로 넘겨받은 이미지 배열값을 저장
            ivImage.setImageBitmap(bmp); // 이미지뷰에 표시해준다.
            sending_bitmap = bmp; // 인텐트로 넘겨받은 이미지배열을 비트맵으로 변환해둔걸 초기화해준다. 그렇지 않으면 이미지 변경이 없을시 에러난다.
        }*/

        diary.setText(day.diary);

    }

    protected void onClick_modi_cancel(View v){ // 확인 버튼을 눌렀을때 이전 액티비티로 이동
        finish();
    }



    protected void onClick_change(View v){
        switch (v.getId()){
            case R.id.pay_ch: // 시급 변경을 누른 경우

                //시급변경 관련 코드
                ad = new AlertDialog.Builder(Detail_history_check_modi.this);
                ad.setTitle("시급 입력");
                ad.setMessage("수정할 시급을 입력해 주세요.");
                final EditText et = new EditText(Detail_history_check_modi.this);
                et.setText(String.valueOf(hour_pay));
                ad.setView(et);
                et.setInputType(InputType.TYPE_CLASS_NUMBER); // 숫자만 입력받도록
                ad.setPositiveButton("변경", new DialogInterface.OnClickListener() {
                    @SuppressLint("DefaultLocale")
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        hour_pay = Integer.valueOf(et.getText().toString());
                        ip.setText(String.format("%d 원",hour_pay));
                        dialog.dismiss();
                    }
                });

                ad.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                ad.show();
                break;
            case R.id.st_ch: // 시작시간 변경을 누른 경우
                new TimePickerDialog(this, mTimeSetListener, mHour_st, mMinute_st, true).show();

                break;
            case R.id.et_ch: // 끝나는 시간 변경을 누른 경우
                new TimePickerDialog(this, mTimeSetListener2, mHour_et, mMinute_et, true).show();

                break;



        }


    }

    TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @SuppressLint("DefaultLocale")
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            mHour_st = hourOfDay;
            mMinute_st = minute;

            ist.setText(String.format("%02d:%02d", mHour_st, mMinute_st));
            int working_min = Place_add.Calculate_time(mHour_st, mMinute_st, mHour_et, mMinute_et);
            iwt.setText(String.format("%d시간 %d분", working_min/60, working_min%60));

        }
    };

    TimePickerDialog.OnTimeSetListener mTimeSetListener2 = new TimePickerDialog.OnTimeSetListener() {
        @SuppressLint("DefaultLocale")
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            mHour_et = hourOfDay;
            mMinute_et = minute;

            iet.setText(String.format("%02d:%02d", mHour_et, mMinute_et));
            int working_min = Place_add.Calculate_time(mHour_st, mMinute_st, mHour_et, mMinute_et);
            iwt.setText(String.format("%d시간 %d분", working_min/60, working_min%60));


        }
    };

    public String saveBitmapToJpeg(Bitmap bitmap){

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()); // 이미지 파일 이름에 시간스템프넣기 위한 부분
        String imageFileName = "Resize_" + timeStamp;

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File tempFile = new File(storageDir, imageFileName);

        try{
            tempFile.createNewFile();  // 파일을 생성해주고
            FileOutputStream out = new FileOutputStream(tempFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90 , out);  // 넘거 받은 bitmap을 jpeg(손실압축)으로 저장해줌
            out.close(); // 마무리로 닫아줍니다.

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tempFile.getAbsolutePath();   // 임시파일 저장경로를 리턴해주면 끝!
    }

    protected void onClick_modi_save(View v){ // 저장버튼을 눌렀을때 자신을 호출한 액티비티에 데이터를 보내주기 위한 부분
        Intent intent = getIntent(); // 자신을 호출한 액티비티의 정보를 얻는다.
        // 입력한 데이터를 저장할 객체를 생성, 이 객체는 하루의 내용에 관련된 객체

       /* if(sending_bitmap != null) {
            sending_bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
            bytes = stream.toByteArray();
        }*/

       if(sending_bitmap != null) {
           SendingPhotoPath = saveBitmapToJpeg(sending_bitmap);
           day.daily_image_path = SendingPhotoPath;
       }




        day.year = input_year;
        day.month = input_month;
        day.day_of_month = input_day_of_month;
        day.Day_start_time_hour = mHour_st;
        day.Day_start_time_min = mMinute_st;
        day.Day_end_time_hour = mHour_et;
        day.Day_end_time_min =  mMinute_et;
        day.hour_pay = hour_pay;
        day.day_of_week = day_of_week;
        day.diary = diary.getText().toString();

        //day.daily_image = bytes;
        day.Calculate();

        String Json = gson.toJson(day);
        Editor.putString(Day_id, Json.toString());
        Editor.commit();

        //intent.putExtra("day_modi_info", day);
        setResult(RESULT_OK, intent);
        finish();
    }


    private void getCameraPermission(){
        String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){

            }else{
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }

    }

    public void d_selectPhoto(View v){
        /*String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {*/
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {

            }
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(this, getPackageName(), photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(intent, CAMERA_CODE);
            }
        }

        //   }

    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "TEST_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,      /* prefix */
                ".jpg",         /* suffix */
                storageDir          /* directory */
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;

    }

    private void getPictureForPhoto() {
        Bitmap bitmap = resize(this, photoUri, 500);
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(currentPhotoPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int exifOrientation;
        int exifDegree;

        if (exif != null) {
            exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            exifDegree = exifOrientationToDegrees(exifOrientation);
        } else {
            exifDegree = 0;
        }
        sending_bitmap = rotate(bitmap, exifDegree);
        ivImage.setImageBitmap(sending_bitmap);//이미지 뷰에 비트맵 넣기
    }


    private int exifOrientationToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    public void d_selectGallery(View v) {

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_CODE);
    }


    private void sendPicture(Uri imgUri) {

        String imagePath = getRealPathFromURI(imgUri); // path 경로
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(imagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        int exifDegree = exifOrientationToDegrees(exifOrientation);

        Bitmap bitmap =  resize(this, imgUri, 500);//경로를 통해 비트맵으로 전환
        sending_bitmap = rotate(bitmap, exifDegree);
        ivImage.setImageBitmap(sending_bitmap);//이미지 뷰에 비트맵 넣기

    }

    private String getRealPathFromURI(Uri contentUri) {
        int column_index=0;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if(cursor.moveToFirst()){
            column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        }

        return cursor.getString(column_index);
    }

    private Bitmap rotate(Bitmap src, float degree) {

// Matrix 객체 생성
        Matrix matrix = new Matrix();
// 회전 각도 셋팅
        matrix.postRotate(degree);
// 이미지와 Matrix 를 셋팅해서 Bitmap 객체 생성
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(),
                src.getHeight(), matrix, true);
    }

    private Bitmap resize(Context context, Uri uri, int resize){
        Bitmap resizeBitmap=null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options); // 1번

            int width = options.outWidth;
            int height = options.outHeight;
            int samplesize = 1;

            while (true) {//2번
                if (width / 2 < resize || height / 2 < resize)
                    break;
                width /= 2;
                height /= 2;
                samplesize *= 2;
            }

            options.inSampleSize = samplesize;
            Bitmap bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options); //3번
            resizeBitmap=bitmap;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return resizeBitmap;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case GALLERY_CODE:
                    sendPicture(data.getData()); //갤러리에서 가져오기
                    break;
                case CAMERA_CODE:
                    getPictureForPhoto();
                    break;
                default:
                    break;
            }

        }
    }
}
