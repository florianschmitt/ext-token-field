/*
 * Copyright 2015 Explicatis GmbH <ext-token-field@explicatis.com>
 * 
 * Author: Florian Schmitt
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

import java.util.List;

import com.explicatis.ext_token_field.shared.Token;
import com.explicatis.ext_token_field.shared.TokenAction;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Label;

@SuppressWarnings("deprecation")
public class TokenWidget extends FocusPanel
{

	public static final String			TOKEN_CLASS_NAME			= "token";
	public static final String			TOKEN_ACTION_CLASS_NAME		= "token-action";
	public static final String			TOKEN_LABEL_CLASS_NAME		= "token-label";
	public static final String			FOCUS_CLASS_NAME			= "focused";
	public static final String			TOKEN_CONTENT_CLASS_NAME	= "token-content";

	private final ExtTokenFieldWidget	extTokenField;
	private final Token					token;
	private final Label					label;
	private final FlowPanel				rootPanel;

	private boolean						isCollapsed					= true;
	private int							cropLabelLength				= 20;

	public TokenWidget(final ExtTokenFieldWidget extTokenField, final Token token, List<TokenAction> tokenActions)
	{
		this.extTokenField = extTokenField;
		this.token = token;

		rootPanel = new FlowPanel();
		rootPanel.getElement().setClassName(TOKEN_CONTENT_CLASS_NAME);

		final Element rootElement = getElement();
		rootElement.setClassName(TOKEN_CLASS_NAME);

		label = new Label();
		label.getElement().setClassName(TOKEN_LABEL_CLASS_NAME);
		label.addClickHandler(labelClickHandler());
		rootPanel.add(label);

		buildTokenActions(tokenActions);

		internalSetLabel();
		add(rootPanel);
	}

	private void buildTokenActions(List<TokenAction> tokenActions)
	{
		if (tokenActions != null)
		{
			for (TokenAction a : tokenActions)
			{
				buildTokenAction(a);
			}
		}
	}

	private void buildTokenAction(final TokenAction action)
	{
		if ((action.inheritsReadOnlyAndEnabled && !extTokenField.isReadOnly() && extTokenField.isEnabled()) || !action.inheritsReadOnlyAndEnabled)
		{
			Anchor actionAnchor = new Anchor(action.label);
			actionAnchor.getElement().setClassName(TOKEN_ACTION_CLASS_NAME);
			rootPanel.add(actionAnchor);
			actionAnchor.addClickHandler(new ClickHandler()
			{

				@Override
				public void onClick(ClickEvent event)
				{
					onTokenActionClicked(action);
				}
			});

			buildIcon(action, actionAnchor);
		}
	}

	protected void buildIcon(final TokenAction action, final Anchor actionAnchor)
	{

	}

	public Token getToken()
	{
		return token;
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

	protected void onTokenActionClicked(TokenAction tokenAction)
	{

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
}
