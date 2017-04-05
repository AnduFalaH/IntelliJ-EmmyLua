/*
 * Copyright (c) 2017. tangzx(love.tangzx@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tang.intellij.lua.debugger.attach.value;

import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XValueChildrenList;
import com.intellij.xdebugger.frame.XValueNode;
import com.intellij.xdebugger.frame.XValuePlace;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * Created by tangzx on 2017/4/2.
 */
public class LuaXTable extends LuaXValue {

    private XValueChildrenList childrenList;

    @Override
    public void computePresentation(@NotNull XValueNode xValueNode, @NotNull XValuePlace xValuePlace) {
        xValueNode.setPresentation(null, null, "table", true);
    }

    @Override
    public void doParse(Node node) {
        super.doParse(node);
        childrenList = new XValueChildrenList();
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            switch (item.getNodeName()) {
                case "element":
                    parseChild(item);
                    break;
            }
        }
    }

    private void parseChild(Node childNode) {
        NodeList childNodes = childNode.getChildNodes();
        String key = null;
        LuaXValue value = null;
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            Node content = item.getFirstChild();
            switch (item.getNodeName()) {
                case "key":
                    LuaXValue keyV = LuaXValue.parse(content, process);
                    key = keyV.toKeyString();
                    break;
                case "data":
                    value = LuaXValue.parse(content, process);
                    break;
            }
        }

        if (key != null && value != null)
            childrenList.add(key, value);
    }

    @Override
    public void computeChildren(@NotNull XCompositeNode node) {
        node.addChildren(childrenList, true);
    }
}