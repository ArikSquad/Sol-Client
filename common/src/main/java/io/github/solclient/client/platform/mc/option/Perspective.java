package io.github.solclient.client.platform.mc.option;

import io.github.solclient.client.platform.VirtualEnum;
import io.github.solclient.client.platform.basic.BasicPlatform;

public interface Perspective extends VirtualEnum {

	Perspective FIRST_PERSON = BasicPlatform.perspective("FIRST_PERSON"),
			THIRD_PERSON_BACK = BasicPlatform.perspective("THIRD_PERSON_BACK"),
			THIRD_PERSON_FRONT = BasicPlatform.perspective("THIRD_PERSON_FRONT");

	boolean isFirstPerson();

	boolean isThirdPerson();

	boolean isReversed();

}
