/*
 * Copyright 2015 Florian Schmitt, Explicatis GmbH <florian.schmitt@explicatis.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.explicatis.ext_token_field.client;

import java.util.LinkedList;
import java.util.List;

import com.explicatis.ext_token_field.shared.ExtTokenFieldServerRpc;
import com.explicatis.ext_token_field.shared.Token;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ui.VFilterSelect;
import com.vaadin.shared.Connector;

import elemental.events.KeyboardEvent.KeyCode;

public class ExtTokenFieldWidget extends FlowPanel
{

	public static final String		TOKEN_FIELD_CLASS_NAME	= "exttokenfield";
	private List<TokenWidget>		tokenWidgets			= new LinkedList<>();
	private ExtTokenFieldServerRpc	serverRpc;
	private VFilterSelect			inputFilterSelect;

	public ExtTokenFieldWidget()
	{
		getElement().setClassName(TOKEN_FIELD_CLASS_NAME);
	}

	public void setInputField(Connector inputField)
	{
		if (inputField != null)
		{
			inputFilterSelect = (VFilterSelect) ((ComponentConnector) inputField).getWidget();
			/**
			 * add key down handler, to select the token at the very left of the filter select when left key was pressed
			 */
			inputFilterSelect.tb.addKeyDownHandler(new KeyDownHandler()
			{

				@Override
				public void onKeyDown(KeyDownEvent event)
				{
					if (event.getNativeKeyCode() == KeyCode.LEFT)
					{
						if (ExtTokenFieldWidget.this.tokenWidgets.size() > 0)
						{
							tokenWidgets.get(tokenWidgets.size() - 1).setFocus(true);
						}
					}
				}
			});
			add(inputFilterSelect);
		}
	}

	public void updateTokens(List<Token> tokens)
	{
		// TODO: register changes, not recreate everything
		removeAllToken();
		addTokens(tokens);
	}

	protected TokenWidget buildTokenWidget(Token token)
	{
		final TokenWidget widget = new TokenWidget(token);
		widget.setServerRpc(serverRpc);
		widget.addFocusHandler(new FocusHandler()
		{

			@Override
			public void onFocus(FocusEvent event)
			{
				widget.getElement().addClassName(TokenWidget.FOCUS_CLASS_NAME);
			}
		});

		widget.addBlurHandler(new BlurHandler()
		{

			@Override
			public void onBlur(BlurEvent event)
			{
				widget.getElement().removeClassName(TokenWidget.FOCUS_CLASS_NAME);
			}
		});

		widget.addKeyDownHandler(new KeyDownHandler()
		{

			@Override
			public void onKeyDown(KeyDownEvent event)
			{
				if (event.getNativeKeyCode() == KeyCodes.KEY_LEFT)
				{
					leftKeyDown(widget);
				}
				else if (event.getNativeKeyCode() == KeyCodes.KEY_RIGHT)
				{
					rightKeyDown(widget);
				}
			}
		});
		return widget;
	}

	protected void rightKeyDown(TokenWidget token)
	{
		int indexOf = tokenWidgets.indexOf(token);
		if (indexOf < tokenWidgets.size() - 1)
		{
			TokenWidget tokenWidget = tokenWidgets.get(indexOf + 1);
			tokenWidget.setFocus(true);
		}
		else if (indexOf == tokenWidgets.size() - 1)
		{
			inputFilterSelect.tb.setFocus(true);
		}
	}

	protected void leftKeyDown(TokenWidget token)
	{
		if (tokenWidgets.size() > 1)
		{
			int indexOf = tokenWidgets.indexOf(token);
			if (indexOf > 0)
			{
				TokenWidget tokenWidget = tokenWidgets.get(indexOf - 1);
				tokenWidget.setFocus(true);
			}
		}
	}

	protected void addTokens(List<Token> tokens)
	{
		for (int i = 0; i < tokens.size(); i++)
		{
			Token t = tokens.get(i);
			TokenWidget widget = buildTokenWidget(t);
			tokenWidgets.add(widget);
			insert(widget, i);
		}
	}

	protected void removeAllToken()
	{
		for (TokenWidget t : tokenWidgets)
		{
			remove(t);
		}
		tokenWidgets.clear();
	}

	public void setServerRpc(ExtTokenFieldServerRpc serverRpc)
	{
		this.serverRpc = serverRpc;
	}
}
