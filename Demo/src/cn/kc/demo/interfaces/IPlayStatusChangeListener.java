package cn.kc.demo.interfaces;

import cn.kc.demo.model.FileHeader;

public interface IPlayStatusChangeListener {
	abstract void changePlayStatus(int from, int to);
	
	abstract void changePlayInfo(FileHeader file);
	
	abstract void changeCurPlayTime(int pos);
}
