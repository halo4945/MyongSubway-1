package com.example.myongsubway;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import android.content.Intent;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;

import static android.content.ContentValues.TAG;

public class ShortestPathActivity extends AppCompatActivity {
    private ViewPager2 viewPager;               // 뷰페이저
    private FragmentStateAdapter pagerAdapter;  // 뷰페이저 어댑터
    private TabLayout tabLayout;                // 탭들을 담는 탭 레이아웃
    private final List<String> tabElement = Arrays.asList("최소시간", "최단거리", "최소비용", "최소환승");  // 탭을 채울 텍스트


    private String departure, arrival;              // 출발역과 도착역
    private ArrayList<ArrayList<Integer>> paths;    // 출발역 ~ 도착역의 경로를 저장하는 리스트, 순서대로 최소시간, 최단거리, 최소비용, 최소환승의 경로가 저장됨
    private ArrayList<ArrayList<Integer>> allCosts; // 소요시간, 소요거리, 소요비용, 환승횟수를 저장하는 리스트, 순서대로 최소시간, 최단거리, 최소비용, 환승횟수의 경우가 저장됨
    private ArrayList<ArrayList<Integer>> allLines; // 경로의 각 역의 호선을 저장하는 리스트
    private final int TYPE_COUNT = 4;               // SearchType 의 경우의 수 (최소시간, 최단거리, 최소비용)
    private final int POINT = 100;

    private ImageButton setAlarmButton;             // 도착알람 설정 버튼

    private CustomAppGraph graph;                   // 액티비티 간에 공유되는 데이터를 담는 클래스
    private FirebaseAuth mAuth;                     // 파이어베이스의 uid 를 참조하기 위해 필요한 변수

    private ArrayList<Integer> btnBackgrounds;      // 역을 나타내는 버튼들의 background xml 파일의 id를 저장하는 리스트
    private ArrayList<Integer> lineColors;          // 호선의 색들을 담고있는 리스트

    ImageButton bookmarkButton;                     // 즐겨찾기 등록 버튼
    boolean isSelected = false;                     // 즐겨찾기 버튼이 눌렸는지를 나타내는 상태변수

    boolean isButtonClicked = false;
    CustomAppGraph.SearchType pageType;
    CustomAppGraph.SearchType buttonType;

    Context mContext = this;

    final int IC_ALARM_FOREGROUND = R.mipmap.ic_alarm_foreground;
    final int IC_ALARM_ANOTHER_SELECTED_FOREGROUND = R.mipmap.ic_alarm_another_selected_foreground;



    AlarmManager alarmManager = null;//안지훈
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shortest_path);

        // 초기화
        init();

        // 버튼 리스너 설정
        registerListener();

        // 프래그먼트에서 사용할 데이터를 초기화
        initializeBtnBackgrounds();
        initializeLineColors();

        // 툴바 설정
        setToolbar();

        // 다익스트라 알고리즘을 통해 경로탐색, 3가지 SearchType 을 모두 수행한다.
        dijkstra(graph.getMap().get(departure), CustomAppGraph.SearchType.MIN_TIME);
        dijkstra(graph.getMap().get(departure), CustomAppGraph.SearchType.MIN_DISTANCE);
        dijkstra(graph.getMap().get(departure), CustomAppGraph.SearchType.MIN_COST);
        dijkstra(graph.getMap().get(departure), CustomAppGraph.SearchType.MIN_TRANSFER);

        // 뷰페이저2, 탭레이아웃 설정
        setPagerAndTabLayout();
    }

    // 초기화하는 메소드
    private void init() {
        // 변수  초기화
        graph = (CustomAppGraph) getApplicationContext();       // 액티비티 간에 공유되는 데이터를 담는 클래스의 객체.
        if (graph == null) return;

        paths = new ArrayList<ArrayList<Integer>>(TYPE_COUNT);
        for (int i = 0; i < TYPE_COUNT; i++) {
            paths.add(new ArrayList<Integer>());
        }

        allCosts = new ArrayList<ArrayList<Integer>>(TYPE_COUNT);
        for (int i = 0; i < TYPE_COUNT; i++) {
            allCosts.add(new ArrayList<Integer>(4));
        }

        allLines = new ArrayList<ArrayList<Integer>>(TYPE_COUNT);
        for (int i = 0; i < TYPE_COUNT; i++) {
            allLines.add(new ArrayList<Integer>());
        }

        setAlarmButton = findViewById(R.id.setAlarmButton);
        setAlarmButton.setColorFilter(getResources().getColor(R.color.moreGray, null));

        bookmarkButton = findViewById(R.id.bookmarkButton);
        bookmarkButton.setColorFilter(Color.parseColor("#BEBEBE"));

        // MainActivity 가 전송한 데이터 받기
        Intent intent = getIntent();
        departure = intent.getStringExtra("departureStation");
        arrival = intent.getStringExtra("destinationStation");
        // TODO : 디버깅용 코드
        if (departure == null) {
            departure = "112";
            arrival = "411";
        }

        mAuth = FirebaseAuth.getInstance();

        initBookmarkButton();
    }

    private void initBookmarkButton() {
        if (isContained()) {
            bookmarkButton.setBackgroundResource(R.mipmap.ic_star_selected_foreground);
        } else {
            bookmarkButton.setBackgroundResource(R.mipmap.ic_star_unselected_foreground);
        }
    }

    // 버튼에 클릭리스너를 등록하는 메소드
    private void registerListener() {
        View.OnClickListener onClickListener = new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.bookmarkButton:
                        // TODO : 즐겨찾기 등록 기능

                        if (isContained()) {
                            // 이미 켜져있을 때, 버튼의 이미지를 빈 별의 이미지로 바꾼다.
                            bookmarkButton.setBackgroundResource(R.mipmap.ic_star_unselected_foreground);
                            // 해당 경로의 즐겨찾기를 제거한다.
                            removeBookmarkedRoute();
                        } else {
                            // 이미 꺼져있을 때, 버튼의 이미지를 노란 별의 이미지로 바꾼다.
                            bookmarkButton.setBackgroundResource(R.mipmap.ic_star_selected_foreground);
                            // 해당 경로의 즐겨찾기를 추가한다.
                            addBookmarkedRoute();
                        }
                        break;


                        case R.id.setAlarmButton:
                            // TODO : 알람 설정 기능

                            if (!isButtonClicked) {//처음 버튼 안지훈
                                buttonType = pageType;
                                isButtonClicked = true;
                                //안지훈
                                Integer cost = allCosts.get(buttonType.ordinal()).get(0);//시간

                                alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);//알람매니저를 통해 알람설정
                                Calendar calendar = Calendar.getInstance();//시간 포멧
                                calendar.setTimeInMillis(System.currentTimeMillis() + cost * 1000); //알람 시간
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.KOREA);//시간확인용 date 포멧
                                Log.i("format", departure + "->" + arrival + (sdf.format(calendar.getTime()).toString()));

                                Intent intent = new Intent(mContext, AlarmReceiver.class);
                                PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 1, intent, 0);//시간이 되면 인텐트할 것


                                //RTC_WAKE : 지정된 시간에 기기의 절전 모드를 해제하여 대기 중인 인텐트를 실행
                                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                                //끝

                                // 버튼의 모양과 색을 눌려져 있는 상태의 경우로 바꾼다.
                                setAlarmButton.setBackgroundResource(R.drawable.bg_white_ripple_stroke_red);
                                setAlarmButton.setColorFilter(Color.WHITE);
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                if (pageType == buttonType) {
                                    // 버튼을 눌렀던 페이지
                                    // 알람 해제 ... 해제할건지 물어봄
                                    builder.setMessage("설정된 알람을 해제하시겠습니까?");
                                    builder.setPositiveButton("확인",
                                            new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            isButtonClicked = false;

                                            // 버튼의 모양과 색을 원래대로 돌린다.
                                            //여기서 알람 취소 안지훈
                                            Intent intent = new Intent(mContext, AlarmReceiver.class);
                                            PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 1, intent, 0);
                                            alarmManager.cancel(pendingIntent);
                                            Log.i("format","double_click cancel");
                                            //끝끝
                                           setAlarmButton.setBackgroundResource(R.drawable.bg_white_ripple_stroke);
                                            setAlarmButton.setImageResource(R.mipmap.ic_alarm_foreground);
                                            setAlarmButton.setColorFilter(getResources().getColor(R.color.moreGray, null));
                                        }
                                    });
                                    builder.setNegativeButton("취소",
                                            new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    });

                                    AlertDialog alert = builder.create();
                                    alert.setOnShowListener(new DialogInterface.OnShowListener() {
                                        @Override
                                        public void onShow(DialogInterface dialog) {
                                            alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
                                            alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLUE);
                                           // alarmManager.cancel();
                                        }
                                    });
                                    alert.show();
                                } else {
                                    // 버튼을 눌렀던 페이지와 다른 페이지
                                    // 다른 페이지에서 이미 알람을 등록한 상태이므로 새로 등록할 것인지 물어봄
                                    builder.setMessage("기존의 알람을 해제하고 새 알람을 등록하시겠습니까?");
                                    builder.setPositiveButton("확인",
                                            new DialogInterface.OnClickListener() {
                                        //여기서 취소 안지훈
                                        public void onClick(DialogInterface dialog, int which) {
                                            buttonType = pageType;

                                            Integer cost = allCosts.get(buttonType.ordinal()).get(0);

                                            alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                                            Calendar calendar = Calendar.getInstance();
                                            calendar.setTimeInMillis(System.currentTimeMillis() + cost * 1000); //알람 시간
                                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.KOREA);
                                            Log.i("format", departure + "->" + arrival + (sdf.format(calendar.getTime()).toString()));

                                            Intent intent = new Intent(mContext, AlarmReceiver.class);
                                            PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 1, intent, 0);

                                            Log.i("format","new cancel");
                                            //RTC_WAKE : 지정된 시간에 기기의 절전 모드를 해제하여 대기 중인 인텐트를 실행
                                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

                                            // 버튼의 모양과 색을 눌려졌을 때로 바꾼다.
                                            setAlarmButton.setBackgroundResource(R.drawable.bg_white_ripple_stroke_red);
                                            setAlarmButton.setImageResource(IC_ALARM_FOREGROUND);
                                            setAlarmButton.setColorFilter(Color.WHITE);
                                        }
                                    });
                                    builder.setNegativeButton("취소",
                                            new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    });

                                    AlertDialog alert = builder.create();
                                    alert.setOnShowListener(new DialogInterface.OnShowListener() {
                                        @Override
                                        public void onShow(DialogInterface dialog) {
                                            alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
                                            alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLUE);
                                        }
                                    });
                                    alert.show();

                                }
                            }
                            break;
                }
            };
        };
        bookmarkButton.setOnClickListener(onClickListener);
        setAlarmButton.setOnClickListener(onClickListener);
    }

    private void addBookmarkedRoute() {

        String value = departure + "역" + " - " + arrival + "역";
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("subwayData").document(mAuth.getUid());

        ArrayList<String> list = new ArrayList<String>();
        Map map = new HashMap<String, Object>();

        for(int i = 0; i < graph.getBookmarkedRoute().size(); i++){
            list.add(graph.getBookmarkedRoute().get(i));
        }

        list.add(value);
        graph.setBookmarkedRoute(list);

        map = graph.getBookmarkedMap();

        map.put("즐겨찾는 역", graph.getBookmarkedStation());
        map.put("즐겨찾는 경로", graph.getBookmarkedRoute());

        docRef.set(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });
    }

    private void removeBookmarkedRoute() {
        String value = departure + "역" + " - " + arrival + "역";
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        graph.getBookmarkedRoute().remove(value);
        DocumentReference docRef = db.collection("subwayData").document(mAuth.getUid());
        docRef.update("즐겨찾는 경로", FieldValue.arrayRemove(value));
    }

    private boolean isContained() {
        String value = departure + "역" + " - " + arrival + "역";
        return graph.getBookmarkedRoute().contains(value);
    }

    public void setPageType(CustomAppGraph.SearchType type) {
        pageType = type;

        if (isButtonClicked)
        {
            if (buttonType != pageType) {
                // 버튼의 모양과 색을 다른 페이지에서 눌려져 있는 상태의 경우로 바꾼다.
                setAlarmButton.setBackgroundResource(R.drawable.bg_white_ripple_stroke);
                setAlarmButton.setImageResource(IC_ALARM_ANOTHER_SELECTED_FOREGROUND);
                setAlarmButton.setColorFilter(Color.RED);
            } else {
                // 버튼의 모양과 색을 눌려져 있는 상태의 경우로 바꾼다.
                setAlarmButton.setBackgroundResource(R.drawable.bg_white_ripple_stroke_red);
                setAlarmButton.setImageResource(IC_ALARM_FOREGROUND);
                setAlarmButton.setColorFilter(Color.WHITE);
            }
        }
    }

    // 뷰페이저2, 탭레이아웃 설정
    private void setPagerAndTabLayout() {
        // 뷰페이저2와 어댑터를 연결 (반드시 TabLayoutMediator 선언 전에 선행되어야 함)
        viewPager = findViewById(R.id.viewPager);
        pagerAdapter = new VPAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        tabLayout = findViewById(R.id.tabLayout);


        // 뷰페이저2와 탭레이아웃을 연동
        // 탭과 뷰페이저를 연결, 여기서 새로운 탭을 다시 만드므로 레이아웃에서 꾸미지말고 여기서 꾸며야함
        new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull @NotNull TabLayout.Tab tab, int position) {
                // 탭의 텍스트를 나타낼 텍스트뷰를 만든다.
                // 텍스트뷰의 정렬, 색을 정하고 탭에 적용시킨다.
                TextView textView = new TextView(ShortestPathActivity.this);
                textView.setText(tabElement.get(position));
                textView.setGravity(Gravity.CENTER);
                textView.setTextColor(getColor(R.color.tabUnSelectedColor));
                if (position == 0) textView.setTextColor(getColor(R.color.tabSelectedColor));
                tab.setCustomView(textView);
            }
        }).attach();

        // 탭이 선택됐을 때의 액션을 설정하는 부분
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) { // 선택 X -> 선택 O
                TextView textView = (TextView) tab.getCustomView();
                textView.setTextColor(getColor(R.color.tabSelectedColor));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { // 선택 O -> 선택 X
                TextView textView = (TextView) tab.getCustomView();
                textView.setTextColor(getColor(R.color.tabUnSelectedColor));

            }

            public void onTabReselected(TabLayout.Tab tab) { // 선택 O -> 선택 O

            }
        });
    }

    // 툴바를 설정하는 메소드
    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("길찾기");
        toolbar.setTitleTextColor(Color.BLACK);
        setSupportActionBar(toolbar);
    }

    // 툴바의 액션버튼을 설정하는 메소드
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.quit_menu, menu);

        Drawable drawable = menu.getItem(0).getIcon();
        drawable.setColorFilter(new BlendModeColorFilter(ContextCompat.getColor(this, R.color.black), BlendMode.SRC_ATOP));
        return true;
    }

    // 툴바의 액션버튼이 선택됐을때의 기능을 설정하는 메소드
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }

    // 경로를 계산하는 다익스트라 알고리즘
    private void dijkstra(int here, CustomAppGraph.SearchType TYPE) {
        // 역과 비용을 관리하는 VertexCost 클래스
        class VertexCost implements Comparable<VertexCost> {
            int vertex;     // 역
            int cost;       // 비용

            public VertexCost(int _vertex, int _cost) {
                vertex = _vertex;
                cost = _cost;
            }

            @Override
            public int compareTo(VertexCost vc) {
                if (this.cost >= vc.cost)
                    return 1;
                else
                    return -1;
            }
        }

        // 각 역에서 갈 수 있는 역을 저장하는 리스트
        // 해당 리스트의 역들은 발견만 한 역일뿐 아직 방문하지 않은 상태이다.
        PriorityQueue<VertexCost> discovered = new PriorityQueue<VertexCost>();

        ArrayList<Integer> best = new ArrayList<Integer>(graph.getStationCount());     // 각 역으로 가는 최단거리를 저장하는 리스트
        ArrayList<Integer> parent = new ArrayList<Integer>(graph.getStationCount());   // 각 역의 이전 역을 저장하는 리스트

        // 리스트 초기화
        for (int i = 0; i < graph.getStationCount(); i++) {
            best.add(Integer.MAX_VALUE);
        }
        for (int i = 0; i < graph.getStationCount(); i++) {
            parent.add(-1);
        }

        // 처음 위치의 정보를 저장
        discovered.add(new VertexCost(here, 0));
        best.set(here, 0);
        parent.set(here, here);

        // 출발역부터 갈 수 있는 모든 정점을 탐색한다.
        while (!discovered.isEmpty()) {
            // 발견한 후보 중 cost 가 가장 작은, 방문할 후보를 찾는다.
            VertexCost bestVC = discovered.remove();

            int cost = bestVC.cost;     // 현재 방문할 역까지의 비용을 저장
            here = bestVC.vertex;       // 현재 방문할 역을 찾은 후보로 변경

            // 더 짧은 경로가 존재한다면 스킵 (새로 찾은 현재 역까지의 비용이 기존의 현재 역까지의 비용보다 크면 스킵)
            if (best.get(here) < cost)
                continue;

            // 방문
            // here 에 해당하는 Vertex 객체에서 연결되어 있는 역 정보를 받아와서 방문할 수 있는 모든 역을 발견
            for (int there : graph.getVertices().get(here).getAdjacent()) {

                int nextCost = 0;

                if (TYPE == CustomAppGraph.SearchType.MIN_TRANSFER) {

                    // 현재 역이 출발 역이라면
                    if (parent.get(here) != here) {
                        ArrayList<Integer> prevLines = graph.getVertices().get(parent.get(here)).getLines();
                        ArrayList<Integer> hereLines = graph.getVertices().get(here).getLines();
                        ArrayList<Integer> thereLines = graph.getVertices().get(there).getLines();

                        int hereLine = 0;

                        Loop1 :
                        for (int pLine : prevLines) {
                            for (int hLine : hereLines) {
                                if (pLine == hLine) {
                                    hereLine = pLine;
                                    break Loop1;
                                }
                            }
                        }

                        int thereLine = 0;

                        Loop2 :
                        for (int hLine : hereLines) {
                            for (int tLine : thereLines) {
                                if (hLine == tLine) {
                                    thereLine = hLine;
                                    break Loop2;
                                }
                            }
                        }

                        if (hereLine != thereLine) {
                            // 환승
                            nextCost = best.get(here) + 1;
                        } else {
                            nextCost = best.get(here);
                        }
                    }
                } else {
                    nextCost = best.get(here) + graph.getAdjacent().get(here).get(there).getCost(TYPE);
                }

                // 더 좋은 경로를 과거에 찾았으면 스킵
                if (nextCost >= best.get(there))
                    continue;

                // 현재 역에서 발견한 역을 등록
                discovered.add(new VertexCost(there, nextCost));
                best.set(there, nextCost);
                parent.set(there, here);
            }

            if (TYPE == CustomAppGraph.SearchType.MIN_TRANSFER)
                if ((Integer.parseInt(graph.getReverseMap().get(here)) >= 112 && Integer.parseInt(graph.getReverseMap().get(here)) <= 115) ||
                        (Integer.parseInt(graph.getReverseMap().get(here)) >= 406 && Integer.parseInt(graph.getReverseMap().get(here)) <= 411) ||
                        (Integer.parseInt(graph.getReverseMap().get(here)) >= 801 && Integer.parseInt(graph.getReverseMap().get(here)) <= 803) ||
                        Integer.parseInt(graph.getReverseMap().get(here)) == 901)
                    Log.d("test", "station : " + graph.getReverseMap().get(here) + " parent : " + graph.getReverseMap().get(parent.get(here)) + " here : " + best.get(here));
        }


        // 경로탐색이 끝남

        // paths 중 인자로 전달된 SearchType 에 맞는 리스트에 저장함
        // 도착역부터 각 역에 등록된 parent 를 찾아 출발역까지 거슬러올라감
        int pos = graph.getMap().get(arrival);
        while (true) {
            paths.get(TYPE.ordinal()).add(pos);
            
            // 출발지에 다다름
            if (pos == parent.get(pos))
                break;

            pos = parent.get(pos);
        }

        // 경로가 저장된 리스트를 뒤집는다.
        Collections.reverse(paths.get(TYPE.ordinal()));

        // 각 경로의 역의 호선을 저장한다.
        getPathLines(TYPE);

        // 소요시간, 소요거리, 소요비용을 저장한다.
        calculateAllCosts(paths.get(TYPE.ordinal()), TYPE, best.get(graph.getMap().get(arrival)));
    }

    // 경로의 각 역의 호선을 저장하는 메소드
    private void getPathLines(CustomAppGraph.SearchType TYPE) {
        ArrayList<Integer> path = paths.get(TYPE.ordinal());        // TYPE 에 맞는 경로
        ArrayList<Integer> lines = allLines.get(TYPE.ordinal());    // TYPE 에 맞는 호선 리스트

        for (int index = 0; index < path.size() - 1; index++) {
            int here = path.get(index);
            int there = path.get(index + 1);

            // 현재 역의 호선과 다음 역의 호선 중 같은 호선이 있다면 그 호선이 현재 역의 호선이 됨.
            Loop1 :
            for (int hereLine : graph.getVertices().get(here).getLines()) {
                for (int thereLine : graph.getVertices().get(there).getLines()) {
                    if (hereLine == thereLine) {
                        lines.add(hereLine);
                        break Loop1;
                    }
                }
            }
        }
        
        // 마지막 역의 경우를 처리, 마지막 역 이전 역의 호선과 같은 호선으로 저장
        lines.add(lines.get(lines.size() - 1));
    }

    // 소요시간, 소요거리, 소요비용, 환승횟수를 계산하는 메소드
    private void calculateAllCosts(ArrayList<Integer> path, CustomAppGraph.SearchType TYPE, int best) {
        switch (TYPE) {
            case MIN_TIME:
                allCosts.get(TYPE.ordinal()).add(best);
                allCosts.get(TYPE.ordinal()).add(calculateElapsed(path, CustomAppGraph.SearchType.MIN_DISTANCE));
                allCosts.get(TYPE.ordinal()).add(calculateElapsed(path, CustomAppGraph.SearchType.MIN_COST));
                allCosts.get(TYPE.ordinal()).add(calculateElapsed(TYPE));
                break;

            case MIN_DISTANCE:
                allCosts.get(TYPE.ordinal()).add(calculateElapsed(path, CustomAppGraph.SearchType.MIN_TIME));
                allCosts.get(TYPE.ordinal()).add(best);
                allCosts.get(TYPE.ordinal()).add(calculateElapsed(path, CustomAppGraph.SearchType.MIN_COST));
                allCosts.get(TYPE.ordinal()).add(calculateElapsed(TYPE));
                break;

            case MIN_COST:
                allCosts.get(TYPE.ordinal()).add(calculateElapsed(path, CustomAppGraph.SearchType.MIN_TIME));
                allCosts.get(TYPE.ordinal()).add(calculateElapsed(path, CustomAppGraph.SearchType.MIN_DISTANCE));
                allCosts.get(TYPE.ordinal()).add(best);
                allCosts.get(TYPE.ordinal()).add(calculateElapsed(TYPE));
                break;

            case MIN_TRANSFER:
                allCosts.get(TYPE.ordinal()).add(calculateElapsed(path, CustomAppGraph.SearchType.MIN_TIME));
                allCosts.get(TYPE.ordinal()).add(calculateElapsed(path, CustomAppGraph.SearchType.MIN_DISTANCE));
                allCosts.get(TYPE.ordinal()).add(calculateElapsed(path, CustomAppGraph.SearchType.MIN_COST));
                allCosts.get(TYPE.ordinal()).add(best);
                break;
        }
    }

    // 각각의 소요 cost 를 계산하는 메소드
    private int calculateElapsed(ArrayList<Integer> path, CustomAppGraph.SearchType TYPE) {
        int output = 0;

        for (int pathIndex = 0; pathIndex < path.size() - 1; pathIndex++) {
            output += graph.getAdjacent().get(path.get(pathIndex)).get(path.get(pathIndex + 1)).getCost(TYPE);
        }

        return output;
    }

    private int calculateElapsed(CustomAppGraph.SearchType LINES_TYPE) {
        int output = 0;

        ArrayList<Integer> lines = allLines.get(LINES_TYPE.ordinal());

        for (int pathIndex = 0; pathIndex < lines.size() - 1; pathIndex++) {
            if (lines.get(pathIndex) != lines.get(pathIndex + 1)) {
                output += 1;
            }
        }

        return output;
    }

    // 역정보 프래그먼트를 띄우는 메소드
    public void generateStationInformationFragment(CustomAppGraph.Vertex vertex) {
        // 역정보 프래그먼트를 띄운다.
        StationInformationFragment frag = new StationInformationFragment(vertex, graph, true);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.station_info_fragment_container, frag);
        transaction.addToBackStack(null);
        transaction.commit();
    }
    
    // 확대경로 프래그먼트를 띄우는 메소드
    public void generateStationInformationFragment(ArrayList<Integer> path, ArrayList<Integer> btnBackgrounds, CustomAppGraph.SearchType TYPE) {
        // 역정보 프래그먼트를 띄운다.
        ZoomPathFragment frag = new ZoomPathFragment(path, allLines.get(TYPE.ordinal()), btnBackgrounds, lineColors);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.zoom_path_fragment_container, frag);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    // 호선에 따른 역버튼 배경 xml 을 담는 메소드
    private void initializeBtnBackgrounds() {
        btnBackgrounds = new ArrayList<Integer>(10);
        btnBackgrounds.add(-1);
        btnBackgrounds.add(R.drawable.round_button_1);
        btnBackgrounds.add(R.drawable.round_button_2);
        btnBackgrounds.add(R.drawable.round_button_3);
        btnBackgrounds.add(R.drawable.round_button_4);
        btnBackgrounds.add(R.drawable.round_button_5);
        btnBackgrounds.add(R.drawable.round_button_6);
        btnBackgrounds.add(R.drawable.round_button_7);
        btnBackgrounds.add(R.drawable.round_button_8);
        btnBackgrounds.add(R.drawable.round_button_9);
    }

    // 호선에 따른 색을 담는 메소드
    private void initializeLineColors() {
        lineColors = new ArrayList<Integer>(10);
        lineColors.add(-1);
        lineColors.add(getResources().getColor(R.color.line1Color, null));
        lineColors.add(getResources().getColor(R.color.line2Color, null));
        lineColors.add(getResources().getColor(R.color.line3Color, null));
        lineColors.add(getResources().getColor(R.color.line4Color, null));
        lineColors.add(getResources().getColor(R.color.line5Color, null));
        lineColors.add(getResources().getColor(R.color.line6Color, null));
        lineColors.add(getResources().getColor(R.color.line7Color, null));
        lineColors.add(getResources().getColor(R.color.line8Color, null));
        lineColors.add(getResources().getColor(R.color.line9Color, null));

    }

    // 뷰페이저 어댑터 클래스
    private class VPAdapter extends FragmentStateAdapter {
        private final ArrayList<Fragment> items;

        public VPAdapter(FragmentActivity fa) {
            super(fa);
            items = new ArrayList<Fragment>();
            items.add(new MinTimePathFragment(paths.get(CustomAppGraph.SearchType.MIN_TIME.ordinal()),
                    allLines.get(CustomAppGraph.SearchType.MIN_TIME.ordinal()),
                    allCosts.get(CustomAppGraph.SearchType.MIN_TIME.ordinal()), graph, btnBackgrounds, lineColors));
            items.add(new MinDistancePathFragment(paths.get(CustomAppGraph.SearchType.MIN_DISTANCE.ordinal()),
                    allLines.get(CustomAppGraph.SearchType.MIN_DISTANCE.ordinal()),
                    allCosts.get(CustomAppGraph.SearchType.MIN_DISTANCE.ordinal()), graph, btnBackgrounds, lineColors));
            items.add(new MinCostPathFragment(paths.get(CustomAppGraph.SearchType.MIN_COST.ordinal()),
                    allLines.get(CustomAppGraph.SearchType.MIN_COST.ordinal()),
                    allCosts.get(CustomAppGraph.SearchType.MIN_COST.ordinal()), graph, btnBackgrounds, lineColors));
            items.add(new MinTransferPathFragment(paths.get(CustomAppGraph.SearchType.MIN_TRANSFER.ordinal()),
                    allLines.get(CustomAppGraph.SearchType.MIN_TRANSFER.ordinal()),
                    allCosts.get(CustomAppGraph.SearchType.MIN_TRANSFER.ordinal()), graph, btnBackgrounds, lineColors));
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {  // 포지션마다 있을 fragment 설정
            return items.get(position);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

}
//알람 시도
/*Integer cost = allCosts.get(currentPos).get(0); //시간

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis() + cost * 1000); //알람 시간
        //1000
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.KOREA);
        Log.i("format", sdf.format(calendar.getTime()).toString());
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(getApplicationContext(),
        1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);

        Toast.makeText(getApplicationContext(), sdf.format(calendar.getTime()), Toast.LENGTH_LONG).show(); */