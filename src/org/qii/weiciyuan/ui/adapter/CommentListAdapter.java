package org.qii.weiciyuan.ui.adapter;

import android.content.Intent;
import android.content.res.TypedArray;
import android.support.v4.app.FragmentActivity;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.CommentBean;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.support.lib.UpdateString;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.Abstract.ICommander;
import org.qii.weiciyuan.ui.Abstract.IToken;
import org.qii.weiciyuan.ui.userinfo.UserInfoActivity;
import org.qii.weiciyuan.ui.widgets.PictureDialogFragment;

import java.util.List;

/**
 * User: qii
 * Date: 12-9-8
 */
public class CommentListAdapter extends BaseAdapter {

    private FragmentActivity activity;
    private LayoutInflater inflater;
    private List<CommentBean> bean;
    private ListView listView;
    private ICommander commander;
    private boolean showOriStatus = true;

    private int checkedBG;
    private int defaultBG;

    public CommentListAdapter(FragmentActivity activity, ICommander commander, List<CommentBean> bean, ListView listView, boolean showOriStatus) {
        this.activity = activity;
        this.inflater = activity.getLayoutInflater();
        this.bean = bean;
        this.commander = commander;
        this.listView = listView;
        this.showOriStatus = showOriStatus;

        int[] attrs = new int[]{R.attr.listview_checked_color};
        TypedArray ta = activity.obtainStyledAttributes(attrs);
        checkedBG = ta.getColor(0, 430);
        defaultBG = activity.getResources().getColor(R.color.transparent);

    }

    private List<CommentBean> getList() {
        return bean;
    }

    public boolean hasStableIds() {
        return true;
    }

    @Override
    public int getCount() {

        if (getList() != null) {
            return getList().size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        if (position >= 0 && getList() != null && getList().size() > 0 && position < getList().size())
            return getList().get(position);
        return null;
    }

    @Override
    public long getItemId(int position) {
        if (getList() != null && getList().size() > 0 && position < getList().size())
            return Long.valueOf(getList().get(position).getId());
        else
            return -1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //mylayout time view position have a bug when set avatar view to gone,so init normal layout
        if (bean.get(position).getUser().getId().equals(GlobalContext.getInstance().getCurrentAccountId()) && GlobalContext.getInstance().isEnablePic()) {
            ViewHolder holder;
            if (convertView == null || convertView.getTag(R.drawable.app) == null) {
                convertView = initMylayout(parent);
                holder = buildHolder(convertView);
            } else {
                boolean enableBigPic = (Boolean) convertView.getTag(R.drawable.account_black);
                if (enableBigPic == GlobalContext.getInstance().getEnableBigPic()) {
                    holder = (ViewHolder) convertView.getTag(R.drawable.app);
                } else {
                    convertView = initMylayout(parent);
                    holder = buildHolder(convertView);
                }
            }
            convertView.setTag(R.drawable.app, holder);
            convertView.setTag(R.drawable.account_black, GlobalContext.getInstance().getEnableBigPic());
            bindViewData(holder, position);
            return convertView;
        }

        ViewHolder holder;
        if (convertView == null || convertView.getTag(R.drawable.ic_launcher) == null) {
            convertView = initNormallayout(parent);
            holder = buildHolder(convertView);
        } else {
            boolean enableBigPic = (Boolean) convertView.getTag(R.drawable.account_black);
            if (enableBigPic == GlobalContext.getInstance().getEnableBigPic()) {
                holder = (ViewHolder) convertView.getTag(R.drawable.ic_launcher);
            } else {
                convertView = initNormallayout(parent);
                holder = buildHolder(convertView);
            }
        }
        convertView.setTag(R.drawable.ic_launcher, holder);
        convertView.setTag(R.drawable.account_black, GlobalContext.getInstance().getEnableBigPic());
        bindViewData(holder, position);

        return convertView;
    }

    private View initMylayout(ViewGroup parent) {
        View convertView;
        if (GlobalContext.getInstance().getEnableBigPic()) {
            convertView = inflater.inflate(R.layout.fragment_listview_item_myself_big_pic_layout, parent, false);
        } else {
            convertView = inflater.inflate(R.layout.fragment_listview_item_myself_layout, parent, false);
        }
        return convertView;
    }

    private View initNormallayout(ViewGroup parent) {
        View convertView;
        if (GlobalContext.getInstance().getEnableBigPic()) {
            convertView = inflater.inflate(R.layout.fragment_listview_item_big_pic_layout, parent, false);
        } else {
            convertView = inflater.inflate(R.layout.fragment_listview_item_layout, parent, false);
        }
        return convertView;
    }


    private ViewHolder buildHolder(View convertView) {
        ViewHolder holder = new ViewHolder();
        holder.username = (TextView) convertView.findViewById(R.id.username);
        TextPaint tp = holder.username.getPaint();
        tp.setFakeBoldText(true);
        holder.content = (TextView) convertView.findViewById(R.id.content);
        holder.repost_content = (TextView) convertView.findViewById(R.id.repost_content);
        holder.time = (TextView) convertView.findViewById(R.id.time);
        holder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
        holder.repost_content_pic = (ImageView) convertView.findViewById(R.id.repost_content_pic);
        holder.listview_root = (RelativeLayout) convertView.findViewById(R.id.listview_root);
        return holder;
    }

    private void bindViewData(ViewHolder holder, int position) {

        holder.listview_root.setBackgroundColor(defaultBG);

        if (listView.getCheckedItemPosition() == position + 1)
            holder.listview_root.setBackgroundColor(checkedBG);

        final CommentBean msg = getList().get(position);
        MessageBean repost_msg = msg.getStatus();


        holder.username.setText(msg.getUser().getScreen_name());
        String image_url = msg.getUser().getProfile_image_url();
        if (!TextUtils.isEmpty(image_url)) {
            commander.downloadAvatar(holder.avatar, msg.getUser().getProfile_image_url(), position, listView);
            holder.avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity, UserInfoActivity.class);
                    intent.putExtra("token", ((IToken) activity).getToken());
                    intent.putExtra("user", msg.getUser());
                    activity.startActivity(intent);
                }
            });
        }
        holder.content.setTextSize(GlobalContext.getInstance().getFontSize());
        holder.content.setText(msg.getListViewSpannableString());

        String time = msg.getListviewItemShowTime();
        UpdateString updateString = new UpdateString(time, holder.time, msg, activity);
        if (!holder.time.getText().toString().equals(time)) {
            holder.time.setText(updateString);
        }
        holder.time.setTag(msg.getId());

        holder.repost_content.setVisibility(View.GONE);
        holder.repost_content_pic.setVisibility(View.GONE);

        if (repost_msg != null && showOriStatus) {
            buildRepostContent(repost_msg, holder, position);
        }


    }

    private void buildRepostContent(final MessageBean repost_msg, ViewHolder holder, int position) {
        holder.repost_content.setVisibility(View.VISIBLE);
        if (repost_msg.getUser() != null) {
            holder.repost_content.setTextSize(GlobalContext.getInstance().getFontSize());
            holder.repost_content.setText(repost_msg.getListViewSpannableString());
        } else {
            holder.repost_content.setText(repost_msg.getText());

        }
        if (!TextUtils.isEmpty(repost_msg.getThumbnail_pic())) {
            holder.repost_content_pic.setVisibility(View.VISIBLE);
            String picUrl;
            if (GlobalContext.getInstance().getEnableBigPic()) {
                picUrl = repost_msg.getBmiddle_pic();
            } else {
                picUrl = repost_msg.getThumbnail_pic();
            }
            holder.repost_content_pic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PictureDialogFragment progressFragment = new PictureDialogFragment(repost_msg.getBmiddle_pic());
                    progressFragment.show(activity.getSupportFragmentManager(), "");
                }
            });
            commander.downContentPic(holder.repost_content_pic, picUrl, position, listView);
        }
    }

    static class ViewHolder {
        TextView username;
        TextView content;
        TextView repost_content;
        TextView time;
        ImageView avatar;
        ImageView repost_content_pic;
        RelativeLayout listview_root;
    }

    public void removeItem(final int postion) {
        if (postion >= 0 && postion < bean.size()) {

            Animation anim = AnimationUtils.loadAnimation(
                    activity, R.anim.account_delete_slide_out_right
            );

            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    bean.remove(postion);
                }

                @Override
                public void onAnimationEnd(Animation animation) {

                    CommentListAdapter.this.notifyDataSetChanged();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            int positonInListView = postion + 1;
            int start = listView.getFirstVisiblePosition();
            int end = listView.getLastVisiblePosition();

            if (positonInListView >= start && positonInListView <= end) {
                int positionInCurrentScreen = postion - start;
                listView.getChildAt(positionInCurrentScreen + 1).startAnimation(anim);
            } else {
                bean.remove(postion);
                CommentListAdapter.this.notifyDataSetChanged();
            }
        }
    }
}

