package com.itheima.mrsbsclient;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.itheima.mrsbsclient.domain.News;
import com.loopj.android.image.SmartImageView;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.util.Xml;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {

	List<News> newsList;
	
	Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			ListView lv = (ListView) findViewById(R.id.lv);
			lv.setAdapter(new MyAdapter());
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		getNewsInfo();
		
//		ListView lv = (ListView) findViewById(R.id.lv);
		//��ʱ������Ϣ��û�н�����ϣ�newsList��û��new����
//		lv.setAdapter(new MyAdapter());
	}
	
	class MyAdapter extends BaseAdapter{
		//���ص�Ҫ��ʾ����Ŀ������
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return newsList.size();
		}
		
		//����һ��View���󣬻���ΪListView��һ����Ŀ��ʾ�ڽ�����
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			News news = newsList.get(position);
			
			View v = null;
			ViewHolder mHolder = null;
			if(convertView == null){
				//�������
				v = View.inflate(MainActivity.this, R.layout.item_listview, null);
				
				//����viewHoler��װ������Ŀʹ�õ����
				mHolder = new ViewHolder();
				mHolder.tv_title = (TextView) v.findViewById(R.id.tv_title);
				mHolder.tv_detail = (TextView) v.findViewById(R.id.tv_detail);
				mHolder.tv_comment = (TextView) v.findViewById(R.id.tv_comment);
				mHolder.siv = (SmartImageView) v.findViewById(R.id.siv);
				
				//��viewHolder��װ��view�����У�����view������ʱ��viewHolderҲ�ͱ�������
				v.setTag(mHolder);
			}
			else{
				v = convertView;
				//��view��ȡ�������viewHolder��viewHolder�о������е�������󣬲���Ҫ��ȥfindViewById
				mHolder = (ViewHolder) v.getTag();
			}
			//����Ŀ�е�ÿ���������Ҫ��ʾ������
			mHolder.tv_title.setText(news.getTitle());
			mHolder.tv_detail.setText(news.getDetail());
			mHolder.tv_comment.setText(news.getComment() + "������");
			
			mHolder.siv.setImageUrl(news.getImageUrl());
			return v;
		}
		
		//����Ŀ��Ҫʹ�õ������������װ���������
		class ViewHolder{
			TextView tv_title;
			TextView tv_detail;
			TextView tv_comment;
			SmartImageView siv;
		}

		@Override
		public Object getItem(int position) {
			return newsList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

	}

	private void getNewsInfo() {
		Thread t = new Thread(){
			@Override
			public void run() {
				String path = "http://192.168.2.174:8080/news.xml";
				try {
					URL url = new URL(path);
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("GET");
					conn.setConnectTimeout(8000);
					conn.setReadTimeout(8000);
					
					if(conn.getResponseCode() == 200){
						//�������Ϣ��һ��xml�ļ����ı���Ϣ����xml������ȥ����������Ҫ��Ϊ�ı�ȥ����
						InputStream is = conn.getInputStream();
						getNewsFromStream(is);
						
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		t.start();
	}
	
	private void getNewsFromStream(InputStream is) {
		//��XML�����л�ȡXML�ļ�����������
		XmlPullParser xp = Xml.newPullParser();
		try {
			xp.setInput(is, "utf-8");
			
			//��ȡ�¼����ͣ�ͨ���¼������жϳ���ǰ�������Ǻ�ʲô�ڵ�
			int type = xp.getEventType();
			
			News news = null;
			while(type != XmlPullParser.END_DOCUMENT){
				switch (type) {
				case XmlPullParser.START_TAG:
					if("newslist".equals(xp.getName())){
						newsList = new ArrayList<News>();
					}
					else if("news".equals(xp.getName())){
						news = new News();
					}
					else if("title".equals(xp.getName())){
						String title = xp.nextText();
						news.setTitle(title);
					}
					else if("detail".equals(xp.getName())){
						String detail = xp.nextText();
						news.setDetail(detail);
					}
					else if("comment".equals(xp.getName())){
						String comment = xp.nextText();
						news.setComment(comment);
					}
					else if("image".equals(xp.getName())){
						String image = xp.nextText();
						news.setImageUrl(image);
					}
					
					break;
				case XmlPullParser.END_TAG:
					if("news".equals(xp.getName())){
						newsList.add(news);
					}
					break;

				}
				//ָ���ƶ�����һ���ڵ㲢�����¼�����
				type = xp.next();
			}
			
			//������Ϣ�������߳�ˢ��listview
			handler.sendEmptyMessage(1);
//			for (News n : newsList) {
//				System.out.println(n.toString());
//			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
