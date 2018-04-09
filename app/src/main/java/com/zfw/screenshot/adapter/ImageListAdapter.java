package com.zfw.screenshot.adapter;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.zfw.screenshot.R;
import com.zfw.screenshot.utils.PxUtils;

import java.io.File;
import java.util.List;

public class ImageListAdapter extends BaseAdapter {

    private List<String> filePathList;

    public ImageListAdapter(List<String> filePathList) {
        this.filePathList = filePathList;
    }

    @Override
    public int getCount() {
        return filePathList == null ? 0 : filePathList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View convertView, final ViewGroup viewGroup) {

        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.img_list_item, null);
            holder = new ViewHolder();
            holder.iv_img = convertView.findViewById(R.id.iv_img);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        int width = PxUtils.ScreenWidth(viewGroup.getContext()) - PxUtils.dip2px(viewGroup.getContext(), 20);
        int height = width - PxUtils.dip2px(viewGroup.getContext(), 60);
        holder.iv_img.getLayoutParams().width = width;
        holder.iv_img.getLayoutParams().height = height;

        int index = filePathList.size() - 1;
        final String filePath = filePathList.get(index - i);

        Glide.with(viewGroup.getContext()).load(filePath).into(holder.iv_img);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent localIntent = new Intent(Intent.ACTION_VIEW);
                localIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    localIntent.setDataAndType(Uri.fromFile(new File(filePath)), "image/*");
                } else {
                    //通过FileProvider创建一个content类型的Uri(android 7.0需要这样的方法跨应用访问)
                    Uri uri = FileProvider.getUriForFile(viewGroup.getContext(), viewGroup.getContext().getPackageName() + ".provider", new File(filePath));
                    localIntent.setDataAndType(uri, "image/*");
                }
                viewGroup.getContext().startActivity(localIntent);
            }
        });

        return convertView;
    }

    static class ViewHolder {
        ImageView iv_img;
    }

}
