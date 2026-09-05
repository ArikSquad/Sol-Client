package io.github.solclient.client.ui.screen.mods;

import java.lang.reflect.*;
import java.net.MalformedURLException;
import java.text.DecimalFormat;

import io.github.solclient.client.mod.ModOption;
import io.github.solclient.client.mod.annotation.Slider;
import io.github.solclient.client.platform.mc.lang.I18n;
import io.github.solclient.client.platform.mc.option.KeyBinding;
import io.github.solclient.client.platform.mc.util.Input;
import io.github.solclient.client.ui.component.*;
import io.github.solclient.client.ui.component.controller.*;
import io.github.solclient.client.ui.component.impl.*;
import io.github.solclient.client.ui.Theme;
import io.github.solclient.client.util.Utils;
import io.github.solclient.client.util.data.*;
import lombok.Getter;

public class ModOptionComponent extends BlockComponent {

	@Getter
	private final ModOption option;
	private boolean listening;
	private int enumWidth;

	public ModOptionComponent(ModOption option) {
		super(Theme.button(), 4F);

		this.option = option;

		add(new LabelComponent((component, defaultText) -> option.getName(), Theme.fg()),
				new AlignedBoundsController(Alignment.CENTRE, Alignment.CENTRE,
						(component, defaultBounds) -> new Rectangle(8, defaultBounds.getY(),
								defaultBounds.getWidth(), defaultBounds.getHeight())));

		Controller<Rectangle> defaultBoundController = new AlignedBoundsController(Alignment.END, Alignment.CENTRE,
				(component, defaultBounds) -> new Rectangle(defaultBounds.getX() - 4,
						defaultBounds.getY(), defaultBounds.getWidth(), defaultBounds.getHeight()));

		if(option.getType() == boolean.class) {
			add(new TickboxComponent((boolean) option.getValue(), option::setValue, this), defaultBoundController);
		}
		else if(option.getType() == Colour.class) {
			add(new ColourBoxComponent((component, defaultColour) -> (Colour) option.getValue(), this),
					defaultBoundController);

			onClick((info, button) -> {
				if(button == 0) {
					Utils.playClickSound(true);
					screen.getRoot().setDialog(new ColourPickerDialog(option, (Colour) option.getValue(), (colour) -> option.setValue(colour)));
					return true;
				}

				return false;
			});
		}
		else if(option.getType() == String.class) {
			TextFieldComponent field = new TextFieldComponent(120, true);
			field.setText((String) option.getValue());
			field.autoFlush().onUpdate((value) -> {
				option.setValue(value);
				return true;
			});
			add(field, new AlignedBoundsController(Alignment.END, Alignment.CENTRE,
					(component, defaultBounds) -> new Rectangle(defaultBounds.getX() - 8,
							defaultBounds.getY(), defaultBounds.getWidth(), defaultBounds.getHeight())));
		}
		else if(option.getType() == KeyBinding.class) {
			KeyBinding binding = (KeyBinding) option.getValue();

			add(new LabelComponent((component, defaultText) -> binding.getBoundKeyName(),
					new AnimatedColourController((component, defaultColour) -> {
						if(listening) {
							return new Colour(255, 255, 85);
						}
						else if(!binding.getConflictingKeys().isEmpty()) {
							return new Colour(255, 85, 85);
						}

						return isHovered() ? Theme.getCurrent().fgButtonHover
								: Theme.getCurrent().fgButton;
					})),
					defaultBoundController);

			onClick((info, button) -> {
				if(button == 0) {
					Utils.playClickSound(true);

					listening = true;
					screen.getRoot().onClickAnwhere((ignoredInfo, pressedButton) -> {
						mc.getOptions().setMouseButton(binding, pressedButton);

						listening = false;

						mc.getOptions().saveFile();
						KeyBinding.reload();

						screen.getRoot().onKeyPressed(null);
						screen.getRoot().onClickAnwhere(null);
						listening = false;
						return true;
					});

					screen.getRoot().onKeyPressed((ignored, code, scancode, mods) -> {
						if(code == Input.ESCAPE) {
							mc.getOptions().unbindKey(binding);
						}
						else if(code != 0) {
							mc.getOptions().setKey(binding, code, scancode);
						}

						listening = false;

						mc.getOptions().saveFile();
						KeyBinding.reload();
						screen.getRoot().onKeyPressed(null);
						screen.getRoot().onClickAnwhere(null);
						return true;
					});
					return true;
				}

				return false;
			});
		}
		else if(option.getType().isEnum()) {
			try {
				Method valuesField = option.getType().getMethod("values");
				valuesField.setAccessible(true);

				Enum<?>[] fields = (Enum<?>[]) valuesField.invoke(null);

				add(new LabelComponent((component, defaultText) -> option.getValue().toString(),
					new AnimatedColourController((component, defaultColour) -> isHovered()
							? Theme.getCurrent().fgButtonHover : Theme.getCurrent().fgButton)),
						(component, defaultBounds) -> {
							if(enumWidth == 0) {
								for(Enum<?> field : fields) {
									int newWidth = (int) font.getTextWidth(field.toString());

									if(newWidth > enumWidth) {
										enumWidth = newWidth;
									}
								}
							}

							Rectangle defaultComponentBounds = defaultBoundController.get(component, defaultBounds);
							return new Rectangle(getBounds().getWidth() - enumWidth - 16 + (enumWidth / 2) - ((int) font.getTextWidth(((LabelComponent) component).getText()) / 2), defaultComponentBounds.getY(), defaultComponentBounds.getWidth(), defaultComponentBounds.getHeight());
						});

				Component previous;

				add(new ScaledIconComponent("sol_client_next", 8, 8, new AnimatedColourController((component,
					defaultColour) -> component.isHovered() ? Theme.getCurrent().fgButtonHover
							: Theme.getCurrent().fgButton)),
						defaultBoundController);

				add(previous = new ScaledIconComponent("sol_client_previous", 8, 8, new AnimatedColourController((component,
					defaultColour) -> component.isHovered() ? Theme.getCurrent().fgButtonHover
							: Theme.getCurrent().fgButton)),
						(component, defaultBounds) -> {
							Rectangle defaultComponentBounds = defaultBoundController.get(component, defaultBounds);
							return new Rectangle(defaultComponentBounds.getX() - enumWidth - 12, defaultComponentBounds.getY(), defaultComponentBounds.getWidth(), defaultComponentBounds.getHeight());
						});

				onClick((info, button) -> {
					if(button == 0) {
						Utils.playClickSound(true);

						int current = ((Enum<?>) option.getValue()).ordinal();

						boolean direction = false;

						if(Input.isShiftDown()) {
							direction = !direction;
						}

						if(previous.isHovered()) {
							direction = !direction;
						}

						if(direction) {
							current--;

							if(current < 0) {
								current = fields.length - 1;
							}
						}
						else {
							current++;
							if(current > fields.length - 1) {
								current = 0;
							}
						}

						option.setValue(fields[current]);

						return true;
					}

					return false;
				});
			}
			catch(NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException error) {
				throw new IllegalStateException(error);
			}
		}
		else if(option.getType() == float.class && option.getField().isAnnotationPresent(Slider.class)) {
			Slider sliderAnnotation = option.getField().getAnnotation(Slider.class);

			if(sliderAnnotation.showValue()) {
				add(new LabelComponent((component, defaultText) -> I18n.translate(sliderAnnotation.format(), new DecimalFormat("0.##").format(option.getValue()))), (component, defaultBounds) -> {
					Rectangle defaultComponentBounds = defaultBoundController.get(component, defaultBounds);
					int sliderX = getBounds().getWidth() - 4 - 100;
					int textWidth = font.getTextWidth(((LabelComponent) component).getText());
					return new Rectangle(sliderX - textWidth - 6, defaultComponentBounds.getY(),
							defaultBounds.getWidth(), defaultBounds.getHeight());
				});
			}

			add(new SliderComponent(sliderAnnotation.min(), sliderAnnotation.max(), sliderAnnotation.step(),
					(float) option.getValue(), (value) -> option.setValue(value), this), (component, defaultBounds) -> {
						defaultBounds = defaultBoundController.get(component, defaultBounds);
						return new Rectangle(defaultBounds.getX() - 5, defaultBounds.getY(), defaultBounds.getWidth(), defaultBounds.getHeight());
					});
		}
		else if(option.isFile()) {
			String text = option.getEditText();

			add(new LabelComponent((component, defaultText) -> text, new AnimatedColourController(
					(component, defaultColour) -> isHovered() ? Theme.getCurrent().fgButtonHover
							: Theme.getCurrent().fgButton)),
					defaultBoundController);

			onClick((info, button) -> {
				if(button == 0) {
					Utils.playClickSound(true);
					try {
						Utils.openUrl(option.getFile().toURI().toURL().toString());
					}
					catch(MalformedURLException error) {
						throw new IllegalStateException(error);
					}
					return true;
				}

				return false;
			});
		}
	}

	@Override
	protected Rectangle getDefaultBounds() {
		return Rectangle.ofDimensions(230, 21);
	}

}
