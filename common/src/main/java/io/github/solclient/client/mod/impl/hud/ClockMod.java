package io.github.solclient.client.mod.impl.hud;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import com.google.gson.annotations.Expose;

import io.github.solclient.client.mod.annotation.Option;
import io.github.solclient.client.mod.hud.SimpleHudMod;

public final class ClockMod extends SimpleHudMod {

	public static final ClockMod INSTANCE = new ClockMod();

	private static final LocalTime PLACEHOLDER = LocalTime.of(13, 30);
	private static final DateTimeFormatter TWELVE_HOUR = DateTimeFormatter.ofPattern("hh:mm a");
	private static final DateTimeFormatter TWENTY_FOUR_HOUR = DateTimeFormatter.ofPattern("HH:mm");

	@Expose
	@Option
	private boolean twentyFourHour = true;

	@Override
	public String getId() {
		return "clock";
	}

	@Override
	public String getText(boolean editMode) {
		LocalTime time = editMode ? PLACEHOLDER : LocalTime.now();
		return (twentyFourHour ? TWENTY_FOUR_HOUR : TWELVE_HOUR).format(time);
	}

}
