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

import com.explicatis.ext_token_field.shared.ExtTokenFieldServerRpc;
import com.explicatis.ext_token_field.shared.Token;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Label;

@SuppressWarnings("deprecation")
public class TokenWidget extends FocusPanel
{

	public static final String		TOKEN_CLASS_NAME		= "token";
	public static final String		TOKEN_LABEL_CLASS_NAME	= "token-label";
	public static final String		TOKEN_REMOVE_CLASS_NAME	= "token-remove";
	public static final String		FOCUS_CLASS_NAME		= "focused";

	private Label					label;

	protected boolean				isCollapsed				= true;
	private int						cropLabelLength			= 20;
	private ExtTokenFieldServerRpc	serverRpc;
	private final Token				token;

	public TokenWidget(Token token)
	{
		this.token = token;

		FlowPanel rootPanel = new FlowPanel();

		final Element rootElement = getElement();
		rootElement.setClassName(TOKEN_CLASS_NAME);

		label = new Label();
		label.getElement().setClassName(TOKEN_LABEL_CLASS_NAME);
		label.addClickHandler(labelClickHandler());
		rootPanel.add(label);

		Anchor removeAnchor = new Anchor("×");
		removeAnchor.getElement().setClassName(TOKEN_REMOVE_CLASS_NAME);
		rootPanel.add(removeAnchor);

		removeAnchor.addClickHandler(removeClickHandler());

		internalSetLabel();
		add(rootPanel);

		addFocusHandler(new FocusHandler()
		{

			@Override
			public void onFocus(FocusEvent event)
			{
				rootElement.addClassName(FOCUS_CLASS_NAME);
			}
		});

		addBlurHandler(new BlurHandler()
		{

			@Override
			public void onBlur(BlurEvent event)
			{
				rootElement.removeClassName(FOCUS_CLASS_NAME);
			}
		});
	}

	protected ClickHandler labelClickHandler()
	{
		return new ClickHandler()
		{

			@Override
			public void onClick(ClickEvent event)
			{
				toggleExpanded();
			}
		};
	}

	protected ClickHandler removeClickHandler()
	{
		return new ClickHandler()
		{

			@Override
			public void onClick(ClickEvent event)
			{
				serverRpc.tokenDeleteClicked(token);
			}
		};
	}

	protected void internalSetLabel()
	{
		if (isCollapsed && token.value != null && token.value.length() > cropLabelLength)
		{
			String substring = token.value.substring(0, cropLabelLength);
			substring += "...";
			label.setText(substring);
		}
		else
		{
			label.setText(token.value);
		}
	}

	public void toggleExpanded()
	{
		if (isCollapsed)
		{
			expand();
		}
		else
		{
			collapse();
		}
		internalSetLabel();
	}

	public void expand()
	{
		isCollapsed = false;
	}

	public void collapse()
	{
		isCollapsed = true;
	}

	public void setServerRpc(ExtTokenFieldServerRpc serverRpc)
	{
		this.serverRpc = serverRpc;
	}
}
