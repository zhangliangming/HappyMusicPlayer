package com.happyplayer.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.happyplayer.common.Constants;
import com.happyplayer.model.Category;
import com.happyplayer.model.SongInfo;
import com.happyplayer.ui.R;

public class CopyOfPlayListAdapter extends BaseExpandableListAdapter {

	/**
	 * 没有子菜单，即是分类名称菜单
	 */
	private final int NOCHILD = 0;
	/**
	 * 有子菜单
	 */
	private final int HASCHILD = 1;

	private LayoutInflater mInflater;
	private List<Category> categorys;

	public CopyOfPlayListAdapter(Context context, List<Category> categorys) {
		this.categorys = categorys;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return getGroup(groupPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		int count = getChildrenCount(groupPosition);
		switch (count) {
		case 0:
			break;
		case 1:
			ChildViewHolder childViewHolder = null;
			if (null == convertView) {
				convertView = mInflater.inflate(
						R.layout.localmusiclist_chliditem, null);
				childViewHolder = new ChildViewHolder(convertView);
				convertView.setTag(childViewHolder);
			} else {
				childViewHolder = (ChildViewHolder) convertView.getTag();
			}
			break;
		}
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		// 异常情况处理
		if (null == categorys || groupPosition < 0
				|| groupPosition > getGroupCount()) {
			return 0;
		}

		int categroyFirstIndex = 0;

		for (Category category : categorys) {
			int size = category.getItemCount();
			// 在当前分类中的索引值
			int categoryIndex = groupPosition - categroyFirstIndex;
			if (categoryIndex == 0) {
				return 0;
			}
			categroyFirstIndex += size;
		}
		return 1;
	}

	@Override
	public Object getGroup(int groupPosition) {
		// 异常情况处理
		if (null == categorys || groupPosition < 0
				|| groupPosition > getGroupCount()) {
			return null;
		}

		// 同一分类内，第一个元素的索引值
		int categroyFirstIndex = 0;

		for (Category category : categorys) {
			int size = category.getItemCount();
			// 在当前分类中的索引值
			int categoryIndex = groupPosition - categroyFirstIndex;
			// item在当前分类内
			if (categoryIndex < size) {
				return category.getItem(categoryIndex);
			}

			// 索引移动到当前分类结尾，即下一个分类第一个元素索引
			categroyFirstIndex += size;
		}

		return null;
	}

	@Override
	public int getGroupType(int groupPosition) {
		// 异常情况处理
		if (null == categorys || groupPosition < 0
				|| groupPosition > getGroupCount()) {
			return NOCHILD;
		}

		int categroyFirstIndex = 0;

		for (Category category : categorys) {
			int size = category.getItemCount();
			// 在当前分类中的索引值
			int categoryIndex = groupPosition - categroyFirstIndex;
			if (categoryIndex == 0) {
				return NOCHILD;
			}
			categroyFirstIndex += size;
		}
		return HASCHILD;

	}

	@Override
	public int getGroupTypeCount() {
		return 2;
	}

	@Override
	public int getGroupCount() {

		int count = 0;

		if (null != categorys) {
			// 所有分类中item的总和是ListVIew Item的总个数
			for (Category category : categorys) {
				count += category.getItemCount();
			}
		}

		return count;
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(final int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {

		int type = getGroupType(groupPosition);
		switch (type) {
		case NOCHILD:
			TViewHolder tViewHolder = null;
			if (null == convertView) {
				convertView = mInflater.inflate(R.layout.category_title, null);
				tViewHolder = new TViewHolder(convertView);
				convertView.setTag(tViewHolder);
			} else {
				tViewHolder = (TViewHolder) convertView.getTag();
			}
			String mCategoryName = (String) getGroup(groupPosition);

			tViewHolder.getcategoryTextTextView().setTextColor(
					Constants.BLACK_GROUND[Constants.DEF_COLOR_INDEX]);
			tViewHolder.getcategoryTextTextView().setText(mCategoryName);
			tViewHolder.getLineView().setBackgroundColor(
					Constants.BLACK_GROUND[Constants.DEF_COLOR_INDEX]);

			break;
		case HASCHILD:
			ViewHolder viewHolder = null;
			if (null == convertView) {

				convertView = mInflater.inflate(R.layout.localmusiclist_item,
						null);
				viewHolder = new ViewHolder(convertView);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			SongInfo songInfo = (SongInfo) getGroup(groupPosition);
			viewHolder.getSongNameTextView().setText(songInfo.getDisplayName());
			if (isExpanded) {
				viewHolder.getItemupImageButton().setVisibility(View.VISIBLE);
				viewHolder.getItemdownImageButton().setVisibility(
						View.INVISIBLE);
			} else {
				viewHolder.getItemupImageButton().setVisibility(View.INVISIBLE);
				viewHolder.getItemdownImageButton().setVisibility(View.VISIBLE);
			}
			viewHolder.getItemupImageButton().setOnClickListener(
					new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							onGroupExpanded(groupPosition);
							notifyDataSetChanged();
						}
					});

			viewHolder.getItemdownImageButton().setOnClickListener(
					new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							onGroupCollapsed(groupPosition);
							notifyDataSetChanged();
						}
					});

			break;
		}

		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	class ChildViewHolder {
		View view;

		ChildViewHolder(View view) {
			this.view = view;
		}

	}

	class TViewHolder {
		View view;
		TextView categoryText;
		View lineView;

		TViewHolder(View view) {
			this.view = view;
		}

		TextView getcategoryTextTextView() {
			if (categoryText == null) {
				categoryText = (TextView) view.findViewById(R.id.category_text);
			}
			return categoryText;
		}

		View getLineView() {
			if (lineView == null) {
				lineView = view.findViewById(R.id.line);
			}
			return lineView;
		}
	}

	class ViewHolder {
		View view;
		TextView songName;

		ImageButton itemupImageButton;
		ImageButton itemdownImageButton;

		ViewHolder(View view) {
			this.view = view;
		}

		ImageButton getItemupImageButton() {
			if (itemupImageButton == null) {
				itemupImageButton = (ImageButton) view
						.findViewById(R.id.item_up);
			}
			return itemupImageButton;
		}

		ImageButton getItemdownImageButton() {
			if (itemdownImageButton == null) {
				itemdownImageButton = (ImageButton) view
						.findViewById(R.id.item_down);
			}
			return itemdownImageButton;
		}

		TextView getSongNameTextView() {
			if (songName == null) {
				songName = (TextView) view.findViewById(R.id.songname);
			}
			return songName;
		}

	}

}
