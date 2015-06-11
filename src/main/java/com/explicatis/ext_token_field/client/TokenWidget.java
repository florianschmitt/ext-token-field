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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;

@SuppressWarnings("deprecation")
public class TokenWidget extends FlowPanel implements HasClickHandlers
{

	public static final String	TOKEN_CLASS_NAME		= "token";
	public static final String	TOKEN_LABEL_CLASS_NAME	= "token-label";
	public static final String	TOKEN_REMOVE_CLASS_NAME	= "token-remove";

	private SimplePanel			label;
	protected boolean			isCollapsed				= true;
	private String				labelText;
	private int					cropLabelLength			= 20;

	public TokenWidget()
	{
		Element rootElement = getElement();
		rootElement.setClassName(TOKEN_CLASS_NAME);

		label = new SimplePanel();
		label.getElement().setClassName(TOKEN_LABEL_CLASS_NAME);
		rootElement.appendChild(label.getElement());

		Element removeAnchor = DOM.createAnchor();
		removeAnchor.setClassName(TOKEN_REMOVE_CLASS_NAME);
		removeAnchor.setInnerText("×");
		rootElement.appendChild(removeAnchor);

		addClickHandler(labelClickHandler());
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

	public void setLabel(String labelText)
	{
		this.labelText = labelText;
		internalSetLabel();
	}

	protected void internalSetLabel()
	{
		if (isCollapsed && labelText.length() > cropLabelLength)
		{
			String substring = labelText.substring(0, cropLabelLength);
			substring += "...";
			label.getElement().setInnerText(substring);
		}
		else
		{
			label.getElement().setInnerText(labelText);
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

	@Override
	public HandlerRegistration addClickHandler(ClickHandler handler)
	{
		return addDomHandler(handler, ClickEvent.getType());
	}
}
