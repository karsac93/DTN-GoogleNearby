package info.androidhive.cardview.interest;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import info.androidhive.cardview.R;

import static android.support.v4.view.MenuItemCompat.getActionView;

public class InterestActivity extends AppCompatActivity implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {
    private Spinner spinner;
    FloatingActionButton fab;
    private List<Interest> interestList;
    private InterestAdapter interestAdapter;
    private RecyclerView interestRecyclerView;
    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interest);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowInterestDialog();
            }
        });

        coordinatorLayout = findViewById(R.id.coordinator);
        interestRecyclerView = findViewById(R.id.interest_recyclerview);
        interestList = new ArrayList<>();
        interestAdapter = new InterestAdapter(this, interestList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        interestRecyclerView.setLayoutManager(layoutManager);
        interestRecyclerView.setItemAnimator(new DefaultItemAnimator());
        interestRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        interestRecyclerView.setAdapter(interestAdapter);
        ItemTouchHelper.SimpleCallback simpleCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(interestRecyclerView);
    }

    private void ShowInterestDialog() {
        LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());
        View view = layoutInflater.inflate(R.layout.interest_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(InterestActivity.this);
        builder.setView(view);
        final EditText editText = (EditText) view.findViewById(R.id.interest);
        TextView dialogTitle = (TextView) view.findViewById(R.id.dialog_title);
        builder
                .setCancelable(false)
                .setPositiveButton("save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {
                        Interest interest = new Interest();
                        interest.setId(1);
                        interest.setInterest(editText.getText().toString());
                        interest.setValue(0.5f);
                        Long ts = System.currentTimeMillis()/1000;
                        interest.setTimestamp(ts.toString());
                        interestList.add(interest);
                        interestAdapter.notifyDataSetChanged();

                    }
                })
                .setNegativeButton("cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                dialogBox.cancel();
                            }
                        });

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.interest_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.spinner);
        spinner = (Spinner) menuItem.getActionView();
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.interest_drop, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int selectionNum, long l) {
                if (selectionNum == 1)
                    fab.setVisibility(View.GONE);
                else
                    fab.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if(viewHolder instanceof InterestAdapter.InterestViewHolder){
            String interest = interestList.get(viewHolder.getAdapterPosition()).getInterest();
            final Interest deletedItem = interestList.get(viewHolder.getAdapterPosition());
            final int deletedIndex = viewHolder.getAdapterPosition();
            interestAdapter.removeItem(viewHolder.getAdapterPosition());

            Snackbar snackbar = Snackbar
                    .make(coordinatorLayout, interest + " removed from cart!", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // undo is selected, restore the deleted item
                    interestAdapter.restoreItem(deletedItem, deletedIndex);
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }
}
