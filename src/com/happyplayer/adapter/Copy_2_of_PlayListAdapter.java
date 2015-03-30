package com.happyplayer.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.happyplayer.common.Constants;
import com.happyplayer.db.SongDB;
import com.happyplayer.model.Category;
import com.happyplayer.model.SongInfo;
import com.happyplayer.model.SongMessage;
import com.happyplayer.observable.ObserverManage;
import com.happyplayer.ui.R;
import com.happyplayer.util.DataUtil;
import com.happyplayer.widget.PlayListItemRelativeLayout;
import com.happyplayer.widget.PlayingImageView;

public class Copy_2_of_PlayListAdapter extends BaseAdapter {

	/**
	 * 标题
	 */
	public final static int CATEGORYTITLE = 0;
	/**
	 * item
	 */
	public final static int ITEM = 1;

	private LayoutInflater mInflater;
	private List<Category> categorys;

	private int playIndexPosition = -1;
	private ListView listView;

	private Context context;

	private boolean isDelete = false;

	private List<String> selectList = new ArrayList<String>();

	public Copy_2_of_PlayListAdapter(Context context, List<Category> categorys,
			ListView listView) {
		this.listView = listView;
		this.categorys = categorys;
		this.context = context;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
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
	public Object getItem(int position) {
		// 异常情况处理
		if (null == categorys || position < 0 || position > getCount()) {
			return null;
		}

		// 同一分类内，第一个元素的索引值
		int categroyFirstIndex = 0;

		for (Category category : categorys) {
			int size = category.getItemCount();
			// 在当前分类中的索引值
			int categoryIndex = position - categroyFirstIndex;
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
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		// 异常情况处理
		if (null == categorys || position < 0 || position > getCount()) {
			return CATEGORYTITLE;
		}

		int categroyFirstIndex = 0;

		for (Category category : categorys) {
			int size = category.getItemCount();
			// 在当前分类中的索引值
			int categoryIndex = position - categroyFirstIndex;
			if (categoryIndex == 0) {
				return CATEGORYTITLE;
			}
			categroyFirstIndex += size;
		}
		return ITEM;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		return getItemViewType(position) != CATEGORYTITLE;
	}

	public boolean isDelete() {
		return isDelete;
	}

	public void setDelete(boolean isDelete) {
		this.isDelete = isDelete;
		for (int i = 0; i < selectList.size(); i++) {
			String sid = selectList.get(i);
			SongDB.getSongInfoDB(context).delete(sid);
		}
	}

	public void setPlayIndexPosition(int playIndexPosition) {
		this.playIndexPosition = playIndexPosition;
	}

	public int getPlayIndexPosition() {
		return playIndexPosition;
	}

	public List<String> getSelectList() {
		return selectList;
	}

	public void setSelectList(List<String> selectList) {
		this.selectList = selectList;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		int type = getItemViewType(position);
		switch (type) {
		case CATEGORYTITLE:
			TViewHolder tViewHolder = null;
			if (null == convertView) {
				convertView = mInflater.inflate(R.layout.category_title, null);
				tViewHolder = new TViewHolder(convertView);
				convertView.setTag(tViewHolder);
			} else {
				tViewHolder = (TViewHolder) convertView.getTag();
			}
			String mCategoryName = (String) getItem(position);

			tViewHolder.getcategoryTextTextView().setTextColor(
					Constants.BLACK_GROUND[Constants.DEF_COLOR_INDEX]);
			tViewHolder.getcategoryTextTextView().setText(mCategoryName);
			tViewHolder.getLineView().setBackgroundColor(
					Constants.BLACK_GROUND[Constants.DEF_COLOR_INDEX]);
			break;
		case ITEM:
			ViewHolder viewHolder = null;
			if (null == convertView) {

				convertView = mInflater.inflate(R.layout.localmusiclist_item,
						null);
				viewHolder = new ViewHolder(convertView);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			final SongInfo songInfo = (SongInfo) getItem(position);
			viewHolder.getSongNameTextView().setText(songInfo.getDisplayName());
			final PlayListItemRelativeLayout playListItemRelativeLayout = viewHolder
					.getListitemBG();
			final PlayingImageView playingImageView = viewHolder
					.getPlayingImageView();
			if (songInfo.getSid().equals(Constants.PLAY_SID)) {
				playIndexPosition = position;
			}
			if (playIndexPosition == position) {
				playListItemRelativeLayout.setSelect(true);
				playingImageView.setVisibility(View.VISIBLE);
			} else {
				playListItemRelativeLayout.setSelect(false);
				playingImageView.setVisibility(View.INVISIBLE);
			}

			final CheckBox selectCheckBox = viewHolder.getSelectCheckBox();
			final ImageButton playAfterImageButton = viewHolder
					.getPlayAfterImageButton();
			if (isDelete) {
				if (selectList.contains(songInfo.getSid())) {
					selectCheckBox.setChecked(true);
				} else {
					selectCheckBox.setChecked(false);
				}
				selectCheckBox.setVisibility(View.VISIBLE);
				playAfterImageButton.setVisibility(View.INVISIBLE);
			} else {
				selectCheckBox.setVisibility(View.INVISIBLE);
				playAfterImageButton.setVisibility(View.VISIBLE);
			}

			playListItemRelativeLayout
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {

							if (isDelete) {
								if (selectCheckBox.isChecked()) {
									selectList.remove(songInfo.getSid());
									selectCheckBox.setChecked(false);
								} else {
									selectList.add(songInfo.getSid());
									selectCheckBox.setChecked(true);
								}
							} else {

								if (playIndexPosition == position) {
									SongMessage songMessage = new SongMessage();
									songMessage
											.setType(SongMessage.PLAYORSTOPMUSIC);
									ObserverManage.getObserver().setMessage(
											songMessage);
									return;
								}
								playListItemRelativeLayout.setSelect(true);
								playingImageView.setVisibility(View.VISIBLE);
								if (playIndexPosition != -1) {
									reshPlayStatusUI(playIndexPosition);
								}
								playIndexPosition = position;
								Constants.PLAY_SID = songInfo.getSid();

								SongMessage songMessage = new SongMessage();
								songMessage.setType(SongMessage.SELECTPLAY);
								ObserverManage.getObserver().setMessage(
										songMessage);

								DataUtil.save(context, Constants.PLAY_SID_KEY,
										Constants.PLAY_SID);

							}
						}
					});

			break;
		}

		return convertView;
	}

	/**
	 * 重新刷新上一次的item页面
	 * 
	 * @param wantedPosition
	 */
	private void reshPlayStatusUI(int wantedPosition) {
		int firstPosition = listView.getFirstVisiblePosition()
				- listView.getHeaderViewsCount();
		int wantedChild = wantedPosition - firstPosition;
		if (wantedChild < 0 || wantedChild >= listView.getChildCount()) {
			return;
		}
		View view = listView.getChildAt(wantedChild);

		ViewHolder holder = (ViewHolder) view.getTag();
		if (holder == null)
			return;
		holder.getListitemBG().setSelect(false);
		holder.getPlayingImageView().setVisibility(View.INVISIBLE);
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
		PlayListItemRelativeLayout listitemBG;
		PlayingImageView playingImageView;

		CheckBox selectCheckBox;
		ImageButton playAfterImageButton;

		ViewHolder(View view) {
			this.view = view;
		}

		ImageButton getPlayAfterImageButton() {
			if (playAfterImageButton == null) {
				playAfterImageButton = (ImageButton) view
						.findViewById(R.id.play_after);
			}
			return playAfterImageButton;
		}

		CheckBox getSelectCheckBox() {
			if (selectCheckBox == null) {
				selectCheckBox = (CheckBox) view.findViewById(R.id.select);
			}
			return selectCheckBox;
		}

		TextView getSongNameTextView() {
			if (songName == null) {
				songName = (TextView) view.findViewById(R.id.songname);
			}
			return songName;
		}

		PlayListItemRelativeLayout getListitemBG() {
			if (listitemBG == null) {
				listitemBG = (PlayListItemRelativeLayout) view
						.findViewById(R.id.listitemBG);
			}
			return listitemBG;
		}

		PlayingImageView getPlayingImageView() {
			if (playingImageView == null) {
				playingImageView = (PlayingImageView) view
						.findViewById(R.id.flag);
			}
			return playingImageView;
		}

	}

}
