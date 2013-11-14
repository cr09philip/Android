package cn.kc.demo.adapter;

import java.util.ArrayList;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListView;

public abstract class ArrayListAdapter<T> extends BaseAdapter {

	protected ArrayList<T> mList;
	protected Activity mContext;
	protected ListView mListView;
	protected LayoutInflater inflater;
	protected String Id;

	public String getId() {
		return Id;
	}

	public void setId(String id) {
		Id = id;
	}

	// 3.0
	private GridView mGridView;

	public void setGridView(GridView gridView) {
		this.mGridView = gridView;
	}

	public GridView getGridView() {
		return mGridView;
	}

	public ArrayListAdapter(LayoutInflater inflater) {
		this.inflater = inflater;
	}

	public ArrayListAdapter(Activity context) {
		this.mContext = context;
		inflater = mContext.getLayoutInflater();
	}

	public ArrayListAdapter() {

	}

	public Activity getmContext() {
		return mContext;
	}

	public void setmContext(Activity mContext) {
		this.mContext = mContext;
	}

	public int getCount() {
		if (mList != null) {
			return mList.size();
		} else
			return 0;
	}

	public Object getItem(int position) {
		return mList == null ? null : mList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	abstract public View getView(int position, View convertView, ViewGroup parent);

	public void setList(ArrayList<T> list) {
		this.mList = list;
		notifyDataSetChanged();
	}

	public ArrayList<T> getList() {
		return mList;
	}

	public void setList(T[] list) {
		ArrayList<T> arrayList = new ArrayList<T>(list.length);
		for (T t : list) {
			arrayList.add(t);
		}
		setList(arrayList);
	}

	public ListView getListView() {
		return mListView;
	}

	public void setListView(ListView listView) {
		mListView = listView;
	}
}
