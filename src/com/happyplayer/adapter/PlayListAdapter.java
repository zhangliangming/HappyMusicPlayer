package com.happyplayer.adapter;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.happyplayer.common.Constants;
import com.happyplayer.model.Category;
import com.happyplayer.model.SongInfo;
import com.happyplayer.model.SongMessage;
import com.happyplayer.observable.ObserverManage;
import com.happyplayer.ui.R;
import com.happyplayer.util.DataUtil;
import com.happyplayer.widget.PlayListItemRelativeLayout;
import com.happyplayer.widget.PlayingImageView;

public class PlayListAdapter extends BaseAdapter implements Observer {

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

	public PlayListAdapter(Context context, List<Category> categorys,
			ListView listView) {
		this.listView = listView;
		this.categorys = categorys;
		this.context = context;
		mInflater = LayoutInflater.from(context);
		ObserverManage.getObserver().addObserver(this);
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

	public void setPlayIndexPosition(int playIndexPosition) {
		this.playIndexPosition = playIndexPosition;
	}

	public int getPlayIndexPosition() {
		return playIndexPosition;
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

			final ImageButton playAfterImageButton = viewHolder
					.getPlayAfterImageButton();

			playListItemRelativeLayout
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {

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
								reshPlayStatusUI(playIndexPosition, false);
							}
							playIndexPosition = position;
							Constants.PLAY_SID = songInfo.getSid();

							SongMessage songMessage = new SongMessage();
							songMessage.setType(SongMessage.SELECTPLAY);
							ObserverManage.getObserver()
									.setMessage(songMessage);

							DataUtil.save(context, Constants.PLAY_SID_KEY,
									Constants.PLAY_SID);

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
	private void reshPlayStatusUI(int wantedPosition, boolean status) {
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
		if (status) {
			holder.getListitemBG().setSelect(true);
			holder.getPlayingImageView().setVisibility(View.VISIBLE);
		} else {
			holder.getListitemBG().setSelect(false);
			holder.getPlayingImageView().setVisibility(View.INVISIBLE);
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
		PlayListItemRelativeLayout listitemBG;
		PlayingImageView playingImageView;

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

	@Override
	public void update(Observable arg0, Object data) {
		if (data instanceof SongMessage) {
			SongMessage songMessage = (SongMessage) data;
			if (songMessage.getType() == SongMessage.NEXTMUSICED
					|| songMessage.getType() == SongMessage.PREVMUSICED
					|| songMessage.getType() == SongMessage.LASTPLAYFINISH
					|| songMessage.getType() == SongMessage.SELECTPLAYED) {
				// System.out.println("zhangliangming------------------>"
				// + songMessage.getType());
				reshNextPlayStatusUI(songMessage.getSongInfo());
			} else if (songMessage.getType() == SongMessage.DEL_NUM) {
				SongInfo songInfo = songMessage.getSongInfo();
				removeItem(songInfo);
				// Category category = categorys.get(0);
				// List<SongInfo> mCategoryItem = new ArrayList<SongInfo>();
				// category.setmCategoryItem(mCategoryItem);
				// categorys.remove(0);
				// categorys.add(0, category);
				// // for (Category iterable_element : categorys) {
				// // categorys.remove(iterable_element);
				// // }
				// notifyDataSetChanged();
			}
		}
	}

	/**
	 * 根据songInfo来移除相关的数据
	 * 
	 * @param songInfo
	 */
	private void removeItem(SongInfo songInfo) {
		// 异常情况处理
		if (null == categorys) {
			return;
		}
		int count = 0;
		for (int i = 0; i < categorys.size(); i++) {
			Category category = categorys.get(i);
			List<SongInfo> songInfos = category.getCategoryItem();
			boolean isRemove = false;
			for (int j = 0; j < songInfos.size(); j++) {
				if (songInfos.get(j).getSid().equals(songInfo.getSid())) {
					if (songInfos.get(j).getSid().equals(Constants.PLAY_SID)) {
						playIndexPosition = -1;
						Constants.PLAY_SID = "";
						reshPlayStatusUI(count + j + 1, false);
					}
					songInfos.remove(j);
					isRemove = true;
					break;
				}
			}
			if (isRemove) {
				if (songInfos.size() == 0) {
					categorys.remove(category);
					notifyDataSetChanged();
					return;
				} else {
					categorys.remove(i);
					category.setmCategoryItem(songInfos);
					categorys.add(i, category);
					notifyDataSetChanged();
					return;
				}

			}
			count += category.getItemCount();
		}
	}

	/**
	 * 刷新下一首的界面
	 * 
	 * @param songInfo
	 */
	private void reshNextPlayStatusUI(SongInfo songInfo) {
		// System.out
		// .println("zhangliangming oldPlayIndexPosition------------------>"
		// + playIndexPosition);
		int oldPlayIndexPosition = playIndexPosition;
		int count = 0;
		if (null != categorys) {
			// 所有分类中item的总和是ListVIew Item的总个数
			for (Category category : categorys) {
				List<SongInfo> songInfos = category.getCategoryItem();
				for (int j = 0; j < songInfos.size(); j++) {
					if (songInfos.get(j).getSid().equals(songInfo.getSid())) {
						playIndexPosition = count + j + 1;
						reshPlayStatusUI(playIndexPosition, true);
						// System.out
						// .println("zhangliangming playIndexPosition------------------>"
						// + playIndexPosition);
						break;
					}
				}
				count += category.getItemCount();
			}
			if (songInfo.getSid().equals("")) {
				playIndexPosition = -1;
				// System.out
				// .println("zhangliangming playIndexPosition2------------------>"
				// + playIndexPosition);
			}
			reshPlayStatusUI(oldPlayIndexPosition, false);
		}
	}
}
