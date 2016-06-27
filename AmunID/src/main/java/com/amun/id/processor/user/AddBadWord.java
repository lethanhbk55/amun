package com.amun.id.processor.user;

import java.util.ArrayList;
import java.util.List;

import com.amun.id.bean.BadWordBean;
import com.amun.id.exception.ExecuteProcessorException;
import com.amun.id.model.BadWordModel;
import com.amun.id.processor.AbstractProcessor;
import com.amun.id.statics.F;
import com.nhb.common.data.MapTuple;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;

public class AddBadWord extends AbstractProcessor {

	@Override
	public PuElement execute(PuObjectRO request) throws ExecuteProcessorException {
		String words = request.getString(F.WORDS);
		String[] array = words.split(",");
		List<BadWordBean> beans = new ArrayList<>();
		for (String word : array) {
			BadWordBean bean = new BadWordBean();
			bean.setWord(word);
			beans.add(bean);
		}
		BadWordModel model = getContext().getModelFactory().newModel(BadWordModel.class);
		model.insert(beans);
		return PuObject.fromObject(new MapTuple<>(F.STATUS, 0));
	}

}
