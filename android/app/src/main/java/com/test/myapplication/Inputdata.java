package com.test.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class Inputdata extends AppCompatActivity {
    TextView date, pn, ip, ist, iet, iwt;
    EditText diary;
    private MainWorkingPlace_Info Place_info; // 인텐트로 전달받은 데이터를 담을 객체변수
    int input_year, input_month, input_day_of_month;
    int mHour_st, mMinute_st, mHour_et, mMinute_et;
    int hour_pay;
    String day_of_week=null;
    AlertDialog.Builder ad; // 시급변경을 할때 띄워주는 창과 관련된 선언부

    //
    ByteArrayOutputStream stream = new ByteArrayOutputStream();

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234; // ?

    ///카메라, 갤러리 관련 변수
    ImageView ivImage;
    private final int CAMERA_CODE = 1111;
    private final int GALLERY_CODE=1112;
    Uri photoUri;
    String currentPhotoPath;
    String sendingPhotoPath;
    Bitmap sending_bitmap = null;
    byte[] bytes = null;


    SharedPreferences mPrefs, nPrefs;
    SharedPreferences.Editor prefsEditor, Editor;
    private Gson gson;
    private String key = "PlaceInfoData";

    String Place_id;
    private Index_day_info day_index;


    // 이 클래스 객체에서 반환해줘야 하는 데이터 = 날짜, 시급, 일하는 시간

        @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inputdata);
        date = (TextView)findViewById(R.id.input_date);
        pn = (TextView)findViewById(R.id.Input_place_name);
        ip = (TextView)findViewById(R.id.Input_pay);
        ist = (TextView)findViewById(R.id.Input_start_time);
        iet = (TextView)findViewById(R.id.Input_end_time);
        iwt = (TextView)findViewById(R.id.Input_working_time);
        ivImage = (ImageView)findViewById(R.id.imageView);
        diary = (EditText)findViewById(R.id.input_diary);


        day_index = new Index_day_info();
        //SharedPreference 관련 코드
        mPrefs = getSharedPreferences("PlaceInfo", MODE_PRIVATE); //현재 근무지의 상태정보는 PlaceInfo라는 xml파일에 SharedPreference를 이용해서 저장해준다.
        prefsEditor = mPrefs.edit(); // sharedPreference를 수정하기 위한 에디터를 연결해준다.
        gson = new Gson(); // 리스트를 통쨰로 저장하기 위한 gson 객체를 생성해준다.

        Place_id = getIntent().getStringExtra("Place id"); // 근무지의 아이디값
        Place_info = get_Place_data();

        nPrefs = getSharedPreferences(Place_id, MODE_PRIVATE); // 해당 근무지의 기록데이터는 근무지 아이디값으로 생성.
        Editor = nPrefs.edit();

        Log.i(getClass().toString(), "받아온 아이템 아이디값 : " + Place_id);

        getCameraPermission();
        init();
    }

    private MainWorkingPlace_Info get_Place_data() {
        String json = mPrefs.getString(Place_id, null);
        return gson.fromJson(json, MainWorkingPlace_Info.class); // 자신을 호출한 액티비티가 보내준 데이터를 저장
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void init(){
            //날짜 부분 초기화
        input_year = Calendar.getInstance().get(Calendar.YEAR);
        input_month = Calendar.getInstance().get(Calendar.MONTH) + 1;
        input_day_of_month = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        switch (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)){
            case Calendar.MONDAY:
                day_of_week = "월요일";
                break;
            case Calendar.TUESDAY:
                day_of_week = "화요일";
                break;
            case Calendar.WEDNESDAY:
                day_of_week = "수요일";
                break;
            case Calendar.THURSDAY:
                day_of_week = "목요일";
                break;
            case Calendar.FRIDAY:
                day_of_week = "금요일";
                break;
            case Calendar.SATURDAY:
                day_of_week = "토요일";
                break;
            case Calendar.SUNDAY:
                day_of_week = "일요일";
                break;
       }
        date.setText(String.format("%d년 %02d월 %02d일 ", input_year, input_month, input_day_of_month) + day_of_week);

        // 그 외의 부분 초기화

        hour_pay = Integer.valueOf(Place_info.Hour_pay);
        mHour_st = Integer.valueOf(Place_info.Start_time_hour);
        mMinute_st = Integer.valueOf(Place_info.Start_time_min);
        mHour_et = Integer.valueOf(Place_info.End_time_hour);
        mMinute_et = Integer.valueOf(Place_info.End_time_min);

        pn.setText(Place_info.Place_name);
        ip.setText(hour_pay + "원");
        ist.setText(String.format("%02d:%02d", mHour_st, mMinute_st));
        iet.setText(String.format("%02d:%02d", mHour_et, mMinute_et));
        iwt.setText(Place_info.Working_time + "시간 " + Place_info.Working_time_min + "분");

    }


    protected void onClick_change(View v){
        switch (v.getId()){
            case R.id.Date_ch: // 날짜 변경을 누른 경우
                new DatePickerDialog(this, mDateListener, input_year, input_month-1, input_day_of_month).show();
                break;

            case R.id.pay_ch: // 시급 변경을 누른 경우

                //시급변경 관련 코드
                ad = new AlertDialog.Builder(Inputdata.this);
                ad.setTitle("시급 입력");
                ad.setMessage("수정할 시급을 입력해 주세요.");
                final EditText et = new EditText(Inputdata.this);
                et.setText(String.valueOf(hour_pay));
                ad.setView(et);
                et.setInputType(InputType.TYPE_CLASS_NUMBER); // 숫자만 입력받도록

                ad.setPositiveButton("변경", new DialogInterface.OnClickListener() {
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

    DatePickerDialog.OnDateSetListener mDateListener = new DatePickerDialog.OnDateSetListener() {
        @SuppressLint("DefaultLocale")
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            input_year = year;
            input_month = month+1;
            input_day_of_month = dayOfMonth;
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            switch (cal.get(Calendar.DAY_OF_WEEK)){
                case Calendar.MONDAY:
                    day_of_week = "월요일";
                    break;
                case Calendar.TUESDAY:
                    day_of_week = "화요일";
                    break;
                case Calendar.WEDNESDAY:
                    day_of_week = "수요일";
                    break;
                case Calendar.THURSDAY:
                    day_of_week = "목요일";
                    break;
                case Calendar.FRIDAY:
                    day_of_week = "금요일";
                    break;
                case Calendar.SATURDAY:
                    day_of_week = "토요일";
                    break;
                case Calendar.SUNDAY:
                    day_of_week = "일요일";
                    break;
            }
            date.setText(String.format("%d년 %02d월 %02d일 ", input_year, input_month, input_day_of_month) + day_of_week);
        }
    };

    protected void onClick_input_cancel(View v){
        finish();
    }

    protected void onClick_save(View v){ // 저장버튼을 눌렀을때 자신을 호출한 액티비티에 데이터를 보내주기 위한 부분

        Intent intent = getIntent(); // 자신을 호출한 액티비티의 정보를 얻는다.
        // 입력한 데이터를 저장할 객체를 생성, 이 객체는 하루의 내용에 관련된 객체

       /* if(sending_bitmap != null){
            sending_bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
             bytes = stream.toByteArray();
        }*/
       if(sending_bitmap != null) {
           sendingPhotoPath = saveBitmapToJpeg(sending_bitmap);
       }

        Day_Info day_info = new Day_Info();
        day_info.year = input_year;
        day_info.month = input_month;
        day_info.day_of_month = input_day_of_month;
        day_info.Day_start_time_hour = mHour_st;
        day_info.Day_start_time_min = mMinute_st;
        day_info.Day_end_time_hour = mHour_et;
        day_info.Day_end_time_min = mMinute_et;
        day_info.hour_pay = hour_pay;
        day_info.day_of_week = day_of_week;
        day_info.diary = diary.getText().toString();
        day_info.daily_image_path = sendingPhotoPath;
        day_info.Calculate();
        //day_info.daily_image = bytes;

        /*intent.putExtra("year", input_year);
        intent.putExtra("month", input_month);
        intent.putExtra("day_of_month", input_day_of_month);
        intent.putExtra("STH", mHour_st);
        intent.putExtra("STM", mMinute_st);
        intent.putExtra("ETH", mHour_et);
        intent.putExtra("ETM", mMinute_et);
        intent.putExtra("pay", hour_pay);
        intent.putExtra("day_of_week", day_of_week);
        intent.putExtra("diary", diary.getText().toString());
        intent.putExtra("image", bytes);*/
        day_info.Day_Id = String.format("%04d%02d%02d", input_year, input_month, input_day_of_month); // 해당 기록의 키값은 연+월+일 값, 중복이 없게 만들기 위함
        Log.i(getClass().toString(), day_info.Day_Id);

        String nl = nPrefs.getString("Index", null);
        if(nl == null){
            Log.i("데이터 추가", "기존 데이터가 없습니다.");
            save_day_info_toSharedPreference(day_info);
            setResult(RESULT_OK, intent);
            finish();
        }else {
            set_Index_day_info();
            boolean flags = true;
            for(int i =0; i< day_index.index.size(); i++){
                if(day_index.index.get(i).equals(day_info.Day_Id)){
                    Toast.makeText(getApplicationContext(), "이미 해당 날짜의 근무 기록이 있습니다.", Toast.LENGTH_SHORT).show();
                    flags = false;
                    break;
                }
            }
            if(flags) {
                save_day_info_toSharedPreference(day_info);
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }

    private void set_Index_day_info() { // 저장된 인덱스값을 불러오는 메소드
        Log.i("데이터 추가", "저장된 인덱스 불러오기");

        try {
            JSONArray jsonArray = new JSONArray(nPrefs.getString("Index", null));
            Log.i("데이터추가" , "불러온 인덱스 : " + jsonArray);
            day_index.index.clear();
            for(int i =0; i < jsonArray.length(); i++){
                String index_data = jsonArray.optString(i);
                day_index.index.add(index_data);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void save_day_info_toSharedPreference(Day_Info day_info) {
        Log.i(getClass().toString(), "근무기록 저장" + day_info.Day_Id);



        String json = gson.toJson(day_info); // 해당 객체를 json String으로 변환시켜준다.
        day_index.index.add(day_info.Day_Id); // 생성된 객체의 아이디값을 저장시켜준다.


        /*int Index = nPrefs.getInt("Size", 0); //근무지의 근무기록 파일에 얼마만큼의 데이터가 담겨있는지 확인하기 위해 사이즈값을 가져온다.
        Editor.putInt("Size", Index+1);*/
        Log.i(getClass().toString(), "저장시 리스트크기 : " + day_index.index.size());



        JSONArray jsonArray = new JSONArray();
        for(int i=0; i<day_index.index.size(); i++){
            jsonArray.put(day_index.index.get(i));
        }
        Log.i(getClass().toString(), "저장된 리스트 : " + jsonArray);


        Editor.putString(day_info.Day_Id, json);
        Editor.putString("Index", jsonArray.toString());
        Editor.commit(); // 해당 변화를 확정지어준다.
        Log.i("근무기록추가", "확정된 인덱스 : " + nPrefs.getString("Index", null));

    }

    //포토, 갤러리

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

    public void selectPhoto(View v){
        /*String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {*/
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                if (photoFile != null) {
                    photoUri = FileProvider.getUriForFile(this, getPackageName(), photoFile);
                    Log.i("photoFile", photoFile.toString());
                    Log.i("photoURI", photoUri.toString());
                    Log.i("currentPath", currentPhotoPath);
                    Log.i("packageName", getPackageName());

                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    startActivityForResult(intent, CAMERA_CODE);

                }
            }

        //}

    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()); // 이미지 파일 이름에 시간스템프넣기 위한 부분
        String imageFileName = "TEST_" + timeStamp;
       /* String imageFileName = "TEST_" + timeStamp + ".jpg"; // 이미지 파일명 규칙 정하기
        File storageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() // 새로운 파일을 생성
        + "/pathvalue/" + imageFileName);
        currentPhotoPath = storageDir.getAbsolutePath(); // 현재 포토의 경로를 저장
        Log.i("mCurrentPhotoPath", currentPhotoPath);


        return storageDir;*/
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                imageFileName,       //prefix
                ".jpg",          //suffix
                storageDir          // directory
        );

        /*File image = new File(storageDir,
                imageFileName);*/

        currentPhotoPath = image.getAbsolutePath();
        Log.i("INPUTDATA", currentPhotoPath);
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
        ivImage.setImageBitmap(sending_bitmap);//카메라에서 가져온 이미지, 이미지 뷰에 비트맵 넣기
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

    public void selectGallery(View v) {

        Intent intent = new Intent(Intent.ACTION_PICK); // 갤러리를 불러온다.
        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI); //
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
        ivImage.setImageBitmap(sending_bitmap);//갤러리에서 가져온 이미지, 이미지 뷰에 비트맵 넣기

    }

    public String getPathFromUri(Uri uri){
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToNext();
        String path = cursor.getString(cursor.getColumnIndex("_data"));
        cursor.close();
        return path;
    }

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


    private void galleyAddPic(){
        Log.i(getClass().toString(), "galleyAddPic");
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
        Toast.makeText(getApplicationContext(), "사진이 저장되었습니다.", Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i(getClass().toString(), "resusltCode" + resultCode +" , "+ RESULT_OK);
        //if (resultCode == RESULT_OK) {

            switch (requestCode) {

                case GALLERY_CODE: // uri값으로 가져온다
                    Log.i(getClass().toString(), "갤러리 값 리턴!!!!!!!!!!");

                   /* Uri uri = data.getData();
                    ivImage.setImageURI(uri);*/
                    sendPicture(data.getData()); //갤러리에서 가져오기
                    break;
                case CAMERA_CODE: // filepath로 가져온다.
                    Log.i(getClass().toString(), "카메라 값 리턴!!!!!!!!!!");
                    getPictureForPhoto();
                    galleyAddPic();

                    break;

                default:
                    break;
            }

       // }
    }
}
