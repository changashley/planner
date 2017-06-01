package frogsolutions.cheesepls;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView navigation;
    private int currentTab;
    private ListView tasks;
    private List<DbHelper> dbs;
    private List<ArrayAdapter<String>> adapters;
    private ArrayAdapter<String> shoppingAdapter;
    private ArrayAdapter<String> eventAdapter;
    private ArrayAdapter<String> todoAdapter;
    private boolean isBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_calendar);
        currentTab = 0;
        dbs = new ArrayList<DbHelper>();
        adapters = new ArrayList<ArrayAdapter<String>>();
        for (int i = 0; i < 3; i++) {
            DbHelper db = new DbHelper(this, Integer.toString(i));
            dbs.add(db);
            adapters.add(new ArrayAdapter<String>(this,R.layout.row,R.id.task_title,db.getTaskList()));
        }
        tasks = (ListView) findViewById(R.id.tasks);
        tasks.setAdapter(adapters.get(0));
        currentTab = 0;
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_calendar:
                        currentTab = 0;
                        tasks.setAdapter(adapters.get(0));
                        return true;
                    case R.id.navigation_shopping:
                        currentTab = 1;
                        tasks.setAdapter(adapters.get(1));
                        return true;
                    case R.id.navigation_todo:
                        currentTab = 2;
                        tasks.setAdapter(adapters.get(2));
                        return true;
                }
                return false;
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);

        //Change menu icon color
        Drawable icon = menu.getItem(0).getIcon();
        icon.mutate();
        icon.setColorFilter(getResources().getColor(R.color.colorPrimaryDark), PorterDuff.Mode.SRC_IN);

        return super.onCreateOptionsMenu(menu);
    }

    private class Event {
        public int month;
        public int day;
        public int year;
        public int hour;
        public int minute;
        public boolean PM;
        public String name;

        @Override
        public String toString() {
            String out = "";
            out += month + "/" + day + "/" + year + " " + hour + ":" + minute + " " + (PM ? "PM" : "AM");
            return out;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(currentTab == 0) {
            final Event event = new Event();
            final DatePicker date = new DatePicker(this);
            final TimePicker time = new TimePicker(this);
            final EditText taskEditText = new EditText(this);
            final AlertDialog dateDialog = new AlertDialog.Builder(this).setTitle(R.string.dialog_pickadate).setView(date)
                    .setPositiveButton(R.string.dialog_next, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            event.year = date.getYear();
                            event.month = date.getMonth();
                            event.day = date.getDayOfMonth();
                            isBack = false;
                        }}).setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            isBack = true;
                        }}).create();
            final AlertDialog timeDialog = new AlertDialog.Builder(this).setTitle(R.string.dialog_pickatime).setView(time)
                    .setPositiveButton(R.string.dialog_next, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            event.hour = time.getCurrentHour();
                            event.minute = time.getCurrentMinute();
                            event.PM = event.hour < 12 ? false : true;
                            event.hour %= 12;
                            if (event.hour == 0) {
                                event.hour = 12;
                            }
                            isBack = false;
                        }}).setNegativeButton(R.string.dialog_back, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            isBack = true;
                            dateDialog.show();
                        }
                    }).create();
            final AlertDialog textDialog = new AlertDialog.Builder(this).setTitle(R.string.dialog_neweventname).setView(taskEditText)
                    .setPositiveButton(R.string.dialog_add, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            event.name = String.valueOf(taskEditText.getText());
                            String name = event.name + "\n" + event.toString();
                            dbs.get(currentTab).insertNewTask(name);
                            adapters.get(currentTab).add(name);
                            adapters.get(currentTab).notifyDataSetChanged();
                            isBack = false;
                        }
                    }).setNegativeButton(R.string.dialog_back, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            isBack = true;
                            timeDialog.show();
                        }
                    }).create();
            dateDialog.show();
            dateDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (!isBack) {
                        timeDialog.show();
                    }
                }
            });
            timeDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (!isBack) {
                        textDialog.show();
                    }
                }
            });
            return true;
        }
        else if (currentTab == 1) {
            final EditText taskEditText = new EditText(this);
            AlertDialog dialog = new AlertDialog.Builder(this).setTitle(R.string.dialog_newshoppingitem).setView(taskEditText)
                    .setPositiveButton(R.string.dialog_add, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String task = String.valueOf(taskEditText.getText());
                            dbs.get(currentTab).insertNewTask(task);
                            adapters.get(currentTab).add(task);
                            adapters.get(currentTab).notifyDataSetChanged();
                        }
                    }).setNegativeButton(R.string.dialog_cancel,null).create();
            dialog.show();
            return true;
        }
        else if (currentTab == 2) {
            final EditText taskEditText = new EditText(this);
            AlertDialog dialog = new AlertDialog.Builder(this).setTitle(R.string.dialog_newtask).setView(taskEditText)
                    .setPositiveButton(R.string.dialog_add, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String task = String.valueOf(taskEditText.getText());
                            dbs.get(currentTab).insertNewTask(task);
                            adapters.get(currentTab).add(task);
                            adapters.get(currentTab).notifyDataSetChanged();
                        }
                    }).setNegativeButton(R.string.dialog_cancel,null).create();
            dialog.show();
            return true;
        }
        else {
            return false;
        }
    }

    public void deleteTask(View view) {
        View parent = (View)view.getParent();
        TextView taskTextView = (TextView)findViewById(R.id.task_title);
        String task = String.valueOf(taskTextView.getText());
        dbs.get(currentTab).deleteTask(task);
        adapters.get(currentTab).remove(task);
        adapters.get(currentTab).notifyDataSetChanged();
    }


}
