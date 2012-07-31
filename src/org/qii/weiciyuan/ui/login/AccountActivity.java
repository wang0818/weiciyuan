package org.qii.weiciyuan.ui.login;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.WeiboAccount;
import org.qii.weiciyuan.support.database.DatabaseManager;
import org.qii.weiciyuan.ui.MainTimeLineActivity;

import java.util.ArrayList;
import java.util.List;

public class AccountActivity extends Activity implements AdapterView.OnItemClickListener {
    /**
     * Called when the activity is first created.
     */

    private ListView listView;

    private AccountAdapter listAdapter;


    private List<WeiboAccount> weiboAccountList = new ArrayList<WeiboAccount>();

    private ActionMode mActionMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {


        //jumpToHomeLine();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.accountactivity_layout);

        listAdapter = new AccountAdapter();
        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(this);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setAdapter(listAdapter);


        listView.setMultiChoiceModeListener(multiChoiceModeLinstener);

        new AccountDBTask().execute(null, null, null);

    }

    private AbsListView.MultiChoiceModeListener multiChoiceModeLinstener = new AbsListView.MultiChoiceModeListener() {

        boolean checkAll = false;

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position,
                                              long id, boolean checked) {


        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_select_all:
                    if (!checkAll) {
                        listAdapter.selectAll();
                        checkAll = true;
                        item.setIcon(R.drawable.accountactivity_select_none);
                    } else {
                        listAdapter.unSelectButRemainCheckBoxAll();
                        checkAll = false;
                        item.setIcon(R.drawable.accountactivity_select_all);
                    }
                    return true;
                default:
                    Toast.makeText(AccountActivity.this, "删除", Toast.LENGTH_SHORT).show();
                    mode.finish();
                    return false;
            }
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.accountactivity_menu_contextual, menu);
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            listAdapter.unSelectAll();
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            listAdapter.addCheckbox();
            return false;
        }
    };

    private void jumpToHomeLine() {
        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        String username = settings.getString("username", "");
        String token = settings.getString("token", "");
        String expires = settings.getString("expires", "");

        boolean haveToken = !TextUtils.isEmpty(token) && !TextUtils.isEmpty(expires);

        boolean haveUsername = !TextUtils.isEmpty(username);

        if (haveToken) {
            Intent intent = new Intent(AccountActivity.this, MainTimeLineActivity.class);
            intent.putExtra("token", token);
            intent.putExtra("expires", expires);

            if (haveUsername) {
                intent.putExtra("username", username);
            }

            startActivity(intent);
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.accountactivity_menu_main, menu);
        return true;
    }

    public void addAccount(MenuItem menu) {

        Intent intent = new Intent(this, OAuthActivity.class);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            new AccountDBTask().execute();

        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        String token = weiboAccountList.get(i).getAccess_token();

        SharedPreferences settings = getPreferences(MODE_PRIVATE);

        SharedPreferences.Editor editor = settings.edit();

        editor.putString("token", token);

        editor.commit();

        Intent intent = new Intent(this, MainTimeLineActivity.class);
        intent.putExtra("token", token);
        intent.putExtra("screen_name",weiboAccountList.get(i).getUsernick());

        startActivity(intent);


    }


    private class AccountAdapter extends BaseAdapter {

        boolean needCheckbox = false;

        boolean allChecked = false;

        @Override
        public int getCount() {
            return weiboAccountList.size();
        }

        @Override
        public Object getItem(int i) {
            return weiboAccountList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            LayoutInflater layoutInflater = getLayoutInflater();

            View mView = layoutInflater.inflate(R.layout.accountactivity_listview_item_layout, viewGroup, false);
            if (needCheckbox) {
                LinearLayout linearLayout = (LinearLayout) mView;

                CheckBox cb = new CheckBox(AccountActivity.this);

                if (allChecked)
                    cb.setChecked(true);

                linearLayout.addView(cb, 0);

            }
            TextView textView = (TextView) mView.findViewById(R.id.textView);

            textView.setText(weiboAccountList.get(i).getUsernick());

            return mView;
        }

        public void addCheckbox() {
            needCheckbox = true;
            notifyDataSetChanged();
        }

        public void removeCheckbox() {
            needCheckbox = false;
            notifyDataSetChanged();
        }

        public void selectAll() {
            needCheckbox = true;
            allChecked = true;
            notifyDataSetChanged();
        }

        public void unSelectAll() {
            needCheckbox = false;
            allChecked = false;
            notifyDataSetChanged();
        }

        public void unSelectButRemainCheckBoxAll() {
            needCheckbox = true;
            allChecked = false;
            notifyDataSetChanged();
        }

    }

    class AccountDBTask extends AsyncTask<Void, List<WeiboAccount>, List<WeiboAccount>> {

        @Override
        protected List<WeiboAccount> doInBackground(Void... params) {
            return DatabaseManager.getInstance().getAccountList();
        }

        @Override
        protected void onPostExecute(List<WeiboAccount> weiboAccounts) {
            weiboAccountList = weiboAccounts;
            listAdapter.notifyDataSetChanged();

        }
    }
}