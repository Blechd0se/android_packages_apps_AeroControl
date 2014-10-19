package com.aero.control.helpers.FileManager;

import android.app.Dialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.aero.control.AeroActivity;
import com.aero.control.R;
import com.aero.control.adapter.AeroData;
import com.aero.control.adapter.FileAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Alexander Christ on 21.09.14.
 */
public class FileManager extends LinearLayout implements OnItemClickListener {

    Context mContext;
    FileManagerListener mFolderListener;
    private static final String mRoot = "/";
    private ListView mListView;
    private String mCurrentPath;
    private Dialog mDialog;
    private List<FileData> mFileData = null;

    /*
     * Holds the meta information of the file manager;
     */
    private class FileData {
        private String item;
        private String path;

        public FileData(String item, String path) {
            this.item = item;
            this.path = path;
        }

    }

    public FileManager(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.mContext = context;

        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.file_folder, this);

        mListView = (ListView) view.findViewById(R.id.list);

        getDir(mRoot, mListView, false);
    }

    public void setIFolderItemListener(FileManagerListener folderItemListener) {
        this.mFolderListener = folderItemListener;
    }

    public void setDir(String dirPath){
        getDir(dirPath, mListView, false);
    }

    public void setDialog(Dialog d) {
        this.mDialog = d;
    }

    private void setTitle(String dirPath) {
        this.mCurrentPath = dirPath;
        if (mDialog != null)
            mDialog.setTitle(mCurrentPath);
    }

    private void getDir(String dirPath, ListView v, boolean root) {

        setTitle(dirPath);
        List<FileData> MetaDirectory = new ArrayList<FileData>();
        List<FileData> MetaFiles = new ArrayList<FileData>();
        String[] rootList;

        File f = new File(dirPath);
        File[] files;
        if (!root) {
            files = f.listFiles();
        } else {
            /*
             * We are going into the root-fallback mode
             * which gives us the full control
             */
            ArrayList<File> rootFiles = new ArrayList<File>();
            rootList = AeroActivity.shell.getRootArray("ls -d " + dirPath + "/*", "\n");
            if (rootList != null) {
                for (String a : rootList) {
                    rootFiles.add(new File(a));
                }
            } else {
                rootFiles.add(new File(mContext.getText(R.string.folder_empty).toString()));
            }
            files = rootFiles.toArray(new File[0]);
        }


        if (!dirPath.equals(mRoot)) {
            MetaDirectory.add(new FileData("../", f.getParent()));
        }


        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isDirectory()) {
                MetaDirectory.add(new FileData(file.getName() + "/", file.getPath()));
            } else {
                MetaFiles.add(new FileData(file.getName(), file.getPath()));
            }
        }

        mFileData = new ArrayList<FileData>();

        // Sort it a little bit;
        Collections.sort(MetaDirectory, new Comparator<FileData>() {
            @Override
            public int compare(FileData fileData, FileData fileData2) {
                return fileData.item.compareTo(fileData2.item);
            }
        });

        Collections.sort(MetaFiles, new Comparator<FileData>() {
            @Override
            public int compare(FileData fileData, FileData fileData2) {
                return fileData.item.compareTo(fileData2.item);
            }
        });

        mFileData.addAll(MetaDirectory);
        mFileData.addAll(MetaFiles);

        setItemList(mFileData);

    }

    public void setItemList(List<FileData> item){
        List<AeroData> mOverviewData= new ArrayList<AeroData>();

        File checkFile;
        for (FileData a : item) {

            // Get the full path;
            checkFile = new File(mCurrentPath + "/" + a.item);

            if (checkFile.isDirectory())
                mOverviewData.add(new AeroData(R.drawable.file_folder, a.item));
            else
                mOverviewData.add(new AeroData(R.drawable.file_document, a.item));
        }

        FileAdapter adapter = new FileAdapter(mContext,
                R.layout.file_row, mOverviewData);

        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(this);
    }


    public void onListItemClick(ListView l, View v, int position, long id) {

        File file = new File(mFileData.get(position).path);

        if (file.isDirectory()) {
            if (file.canRead())
                getDir(file.toString(), l, false);
            else {
                //what to do when folder is unreadable
                if (mFolderListener != null) {
                    mFolderListener.OnCannotFileRead(file);
                }
                // We are root!
                getDir(file.toString(), l, true);

            }
        } else {
            // File is actually clicked;
            if (mFolderListener != null) {
                mFolderListener.OnFileClicked(file);
            }

        }
    }

    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        onListItemClick((ListView) arg0, arg0, arg2, arg3);
    }

}