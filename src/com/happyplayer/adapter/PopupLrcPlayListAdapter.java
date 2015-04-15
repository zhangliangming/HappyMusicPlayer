package com.happyplayer.adapter;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.happyplayer.common.Constants;
import com.happyplayer.manage.MediaManage;
import com.happyplayer.model.SongInfo;
import com.happyplayer.model.SongMessage;
import com.happyplayer.observable.ObserverManage;
import com.happyplayer.ui.R;
import com.happyplayer.util.DataUtil;
import com.happyplayer.util.ImageUtil;
import com.happyplayer.widget.CircleImageView;
import com.happyplayer.widget.PopPlayListItemRelativeLayout;

public class PopupLrcPlayListAdapter extends BaseAdapter implements Observer {
	private List<SongInfo> playlist;
	private int playIndexPosition = -1;
	private Context context;
	private ListView popPlayListView;
	private PopupWindow mPopupWindow;

	public PopupLrcPlayListAdapter(Context context, List<SongInfo> playlist,
			ListView popPlayListView, PopupWindow mPopupWindow) {
		this.playlist = playlist;
		this.context = context;
		this.popPlayListView = popPlayListView;
		this.mPopupWindow = mPopupWindow;
		ObserverManage.getObserver().addObserver(this);
	}

	@Override
	public int getCount() {
		return playlist.size();
	}

	@Override
	public Object getItem(int arg0) {
		return playlist.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup arg2) {
		ViewHolder holder = null;
		if (null == convertView) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.popup_list_lrc_item, null);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		final SongInfo songInfo = playlist.get(position);
		final TextView songNameTextView = holder.getSongNameTextView();
		final TextView singerNameTextView = holder.getSingerNameTextView();
		songNameTextView.setText(songInfo.getTitle());
		singerNameTextView.setText(songInfo.getArtist());

		final CircleImageView singerImageView = holder.getSingerImageView();
		final PopPlayListItemRelativeLayout listitemBG = holder.getListitemBG();
		final TextView songNoTextView = holder.getSongNoTextView();
		songNoTextView.setText((position + 1) + "");
		if (songInfo.getSid().equals(Constants.PLAY_SID)) {
			playIndexPosition = position;
		}
		if (playIndexPosition == position) {
			listitemBG.setSelect(true);
			songNameTextView.setTextColor(Color.WHITE);
			songNoTextView.setVisibility(View.INVISIBLE);
			singerImageView.setVisibility(View.VISIBLE);

			ImageUtil.loadCircleAlbum(context, singerImageView,
					R.drawable.fx_icon_user_default, songInfo.getPath(),
					songInfo.getSid(), songInfo.getDownUrl());

		} else {
			listitemBG.setSelect(false);
			holder.getSongNameTextView().setTextColor(Color.rgb(193, 193, 193));
			songNoTextView.setVisibility(View.VISIBLE);
			singerImageView.setVisibility(View.INVISIBLE);
		}

		holder.getDeleImageView().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				SongMessage songMessage = new SongMessage();
				songMessage.setType(SongMessage.DELMUSIC);
				songMessage.setSongInfo(songInfo);
				ObserverManage.getObserver().setMessage(songMessage);
			}
		});

		listitemBG.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (playIndexPosition == position) {
					SongMessage songMessage = new SongMessage();
					songMessage.setType(SongMessage.PLAYORSTOPMUSIC);
					ObserverManage.getObserver().setMessage(songMessage);
					return;
				}
				listitemBG.setSelect(true);
				songNameTextView.setTextColor(Color.WHITE);

				songNoTextView.setVisibility(View.INVISIBLE);
				singerImageView.setVisibility(View.VISIBLE);
				ImageUtil.loadCircleAlbum(context, singerImageView,
						R.drawable.fx_icon_user_default, songInfo.getPath(),
						songInfo.getSid(), songInfo.getDownUrl());
				if (playIndexPosition != -1) {
					reshPlayStatusUI(playIndexPosition, false, null);
				}
				playIndexPosition = position;
				Constants.PLAY_SID = songInfo.getSid();

				SongMessage songMessage = new SongMessage();
				songMessage.setType(SongMessage.SELECTPLAYED);
				songMessage.setSongInfo(songInfo);
				ObserverManage.getObserver().setMessage(songMessage);

				DataUtil.save(context, Constants.PLAY_SID_KEY,
						Constants.PLAY_SID);
				if (mPopupWindow != null && mPopupWindow.isShowing())
					mPopupWindow.dismiss();
			}
		});

		return convertView;
	}

	/**
	 * 重新刷新上一次的item页面
	 * 
	 * @param wantedPosition
	 */
	private void reshPlayStatusUI(int wantedPosition, boolean status,
			SongInfo songInfo) {
		int firstPosition = popPlayListView.getFirstVisiblePosition()
				- popPlayListView.getHeaderViewsCount();
		int wantedChild = wantedPosition - firstPosition;
		if (wantedChild < 0 || wantedChild >= popPlayListView.getChildCount()) {
			return;
		}
		View view = popPlayListView.getChildAt(wantedChild);

		ViewHolder holder = (ViewHolder) view.getTag();
		if (holder == null)
			return;
		if (status) {
			holder.getListitemBG().setSelect(true);
			holder.getSongNameTextView().setTextColor(Color.rgb(193, 193, 193));
			holder.getSongNoTextView().setVisibility(View.INVISIBLE);
			holder.getSingerImageView().setVisibility(View.VISIBLE);
			ImageUtil.loadCircleAlbum(context, holder.getSingerImageView(),
					R.drawable.fx_icon_user_default, songInfo.getPath(),
					songInfo.getSid(), songInfo.getDownUrl());
		} else {
			holder.getListitemBG().setSelect(false);
			holder.getSongNameTextView().setTextColor(Color.WHITE);
			holder.getSongNoTextView().setVisibility(View.VISIBLE);
			holder.getSingerImageView().setVisibility(View.INVISIBLE);
		}

	}

	@Override
	public void update(Observable arg0, Object data) {
		if (data instanceof SongMessage) {
			SongMessage songMessage = (SongMessage) data;
			if (songMessage.getType() == SongMessage.INIT
					|| songMessage.getType() == SongMessage.LASTPLAYFINISH) {
				reshNextPlayStatusUI(songMessage.getSongInfo());
			} else if (songMessage.getType() == SongMessage.DEL_NUM) {
				notifyDataSetChanged();
			}
		}
	}

	/**
	 * 刷新下一首的界面
	 * 
	 * @param songInfo
	 */
	private void reshNextPlayStatusUI(SongInfo songInfo) {
		int oldPlayIndexPosition = playIndexPosition;
		playIndexPosition = MediaManage.getMediaManage(context).getPlayIndex();
		if (playIndexPosition == oldPlayIndexPosition)
			return;
		if (playIndexPosition != -1) {
			reshPlayStatusUI(playIndexPosition, true, songInfo);
		}
		reshPlayStatusUI(oldPlayIndexPosition, false, null);
		if (playIndexPosition != -1) {
			popPlayListView.setSelection(playIndexPosition);
		}
	}

	private class ViewHolder {
		private View view;
		private TextView songNameTextView;
		private TextView singerNameTextView;
		private TextView songNoTextView;
		private CircleImageView singerImageView;
		private ImageView deleImageView;
		private PopPlayListItemRelativeLayout listitemBG;

		ViewHolder(View v) {
			view = v;
		}

		TextView getSongNameTextView() {
			if (songNameTextView == null) {
				songNameTextView = (TextView) view.findViewById(R.id.song_name);
			}
			return songNameTextView;
		}

		TextView getSingerNameTextView() {
			if (singerNameTextView == null) {
				singerNameTextView = (TextView) view
						.findViewById(R.id.singer_name);
			}
			return singerNameTextView;
		}

		TextView getSongNoTextView() {
			if (songNoTextView == null) {
				songNoTextView = (TextView) view.findViewById(R.id.songno);
			}
			return songNoTextView;
		}

		CircleImageView getSingerImageView() {
			if (singerImageView == null) {
				singerImageView = (CircleImageView) view.findViewById(R.id.pic);
			}
			return singerImageView;
		}

		ImageView getDeleImageView() {
			if (deleImageView == null) {
				deleImageView = (ImageView) view.findViewById(R.id.dele_list);
			}
			return deleImageView;
		}

		PopPlayListItemRelativeLayout getListitemBG() {
			if (listitemBG == null) {
				listitemBG = (PopPlayListItemRelativeLayout) view
						.findViewById(R.id.listitemBG);
			}
			return listitemBG;
		}
	}

}
