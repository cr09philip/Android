package cn.kc.demo.model;

public class FolderModel {
    private String mStrFolderName;

	public FolderModel(String name) {
		mStrFolderName = name;
	}

	public String getFolderName() {
		return mStrFolderName;
	}

	public void setFolderName(String folderName) {
		this.mStrFolderName = folderName;
	} 
}
