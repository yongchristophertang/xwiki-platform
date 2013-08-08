/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.officeimporter.internal.filter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xwiki.component.annotation.Component;
import org.xwiki.xml.html.filter.AbstractHTMLFilter;

/**
 * Filters duplicated anchors.
 * <p>
 * The HTML generated by the office server includes anchors of the form:
 * 
 * <pre>
 * {@code <a name="table1">
 *   <h1>Sheet 1: <em>Hello</em></h1>
 * </a>}
 * </pre>
 * 
 * and the default HTML cleaner converts them to:
 * 
 * <pre>
 * {@code <a name="table1"/>
 * <h1>
 *   <a name="table1">Sheet 1: <em>Hello</em></a>
 * </h1>}
 * </pre>
 * 
 * this is because of the close-before-copy-inside behavior of default HTML cleaner. Thus the additional (copy-inside)
 * anchor needs to be ripped off.
 * 
 * @version $Id$
 * @since 1.8M1
 */
@Component("officeimporter/anchor")
public class AnchorFilter extends AbstractHTMLFilter
{
    @Override
    public void filter(Document document, Map<String, String> cleaningParameters)
    {
        List<Element> links = filterDescendants(document.getDocumentElement(), new String[] {TAG_A});
        Set<String> fragmentIdentifiers = new HashSet<String>();
        List<Element> anchorsToRemove = new ArrayList<Element>();
        for (Element link : links) {
            if (isAnchor(link)) {
                String fragmentIdentifier = link.getAttribute(ATTRIBUTE_NAME);
                if (fragmentIdentifiers.contains(fragmentIdentifier)) {
                    anchorsToRemove.add(link);
                }
                fragmentIdentifiers.add(fragmentIdentifier);
            }
        }
        for (Element anchor : anchorsToRemove) {
            replaceWithChildren(anchor);
        }
    }

    /**
     * Checks whether the given node represents an HTML anchor.
     * 
     * <pre>
     * {@code <a name="Chapter1"/>}
     * </pre>
     * 
     * @param node the {@link Node}
     * @return {@code true} if the node represents an anchor, {@code false} otherwise
     */
    private boolean isAnchor(Node node)
    {
        return node instanceof Element && !"".equals(((Element) node).getAttribute(ATTRIBUTE_NAME));
    }
}
