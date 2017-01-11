package com.explicatis.ext_token_field.client;

import com.explicatis.ext_token_field.shared.DropTargetType;
import com.explicatis.ext_token_field.shared.ExtTokenFieldServerRpc;
import com.explicatis.ext_token_field.shared.Token;
import com.google.gwt.event.dom.client.DragLeaveEvent;
import com.google.gwt.event.dom.client.DragLeaveHandler;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.user.client.ui.SimplePanel;

public class DropTargetWidget extends SimplePanel
{

	public static final String			DROP_TARGET_CLASS_NAME	= "token-drop-target";
	public static final String			HIGHLIGHTED_CLASS_NAME	= "highlighted";

	private final DropTargetType		type;
	private final Token					token;
	private final ExtTokenFieldWidget	fieldWidget;

	public DropTargetWidget(ExtTokenFieldWidget fieldWidget, DropTargetType type, Token token)
	{
		this.fieldWidget = fieldWidget;
		this.type = type;
		this.token = token;

		addStyleName(DROP_TARGET_CLASS_NAME);

		initDragOverHandler();
		initDragLeaveHandler();
		initDropHandler();
	}

	private void setHighlighted(boolean value)
	{
		if (value)
		{
			addStyleName(HIGHLIGHTED_CLASS_NAME);
		}
		else
		{
			removeStyleName(HIGHLIGHTED_CLASS_NAME);
		}
	}

	private void wasDropped(String sourceTokenId)
	{
		Long sourceTokenIdLong = Long.valueOf(sourceTokenId);
		wasDropped(sourceTokenIdLong);
	}

	protected void wasDropped(long sourceTokenId)
	{
		ExtTokenFieldServerRpc serverRpc = fieldWidget.getServerRpc();
		Token sourceToken = fieldWidget.findTokenById(sourceTokenId);
		boolean sourceIsTarget = sourceToken.equals(token);
		if (sourceIsTarget)
			return;

		serverRpc.tokenDroped(sourceToken, token, type);
	}

	private void initDropHandler()
	{
		addDomHandler(new DropHandler()
		{

			@Override
			public void onDrop(DropEvent event)
			{
				String sourcetokenid = event.getData(TokenWidget.SOURCE_TOKEN_ID_PROPERTY);
				wasDropped(sourcetokenid);
				setHighlighted(false);
			}
		}, DropEvent.getType());
	}

	private void initDragLeaveHandler()
	{
		addDomHandler(new DragLeaveHandler()
		{

			@Override
			public void onDragLeave(DragLeaveEvent event)
			{
				setHighlighted(false);
			}
		}, DragLeaveEvent.getType());
	}

	private void initDragOverHandler()
	{
		addDomHandler(new DragOverHandler()
		{

			@Override
			public void onDragOver(DragOverEvent event)
			{
				setHighlighted(true);
			}
		}, DragOverEvent.getType());
	}

}
