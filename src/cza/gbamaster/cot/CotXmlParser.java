package cza.gbamaster.cot;

import cza.util.Pull;

public class CotXmlParser extends Pull {
	private CotResource mCotRes;
	
	public CotXmlParser(CotResource cotRes){
		mCotRes = cotRes;
	}
	
	@Override
	public String getValue(String name) {
		return mCotRes.getString(super.getValue(name));
	}
}
