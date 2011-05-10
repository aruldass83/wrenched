package com.wrenched.example.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.springframework.core.io.DefaultResourceLoader;

import com.wrenched.example.tree.*;

import com.wrenched.core.annotations.LazyAttributeDomain;
import com.wrenched.core.annotations.LazyAttributeFetcher;
import com.wrenched.core.annotations.LazyAttributeProvider;
import com.wrenched.core.annotations.LazyAttributeProviderType;
import com.wrenched.example.domain.TreeNode;

@LazyAttributeProvider(LazyAttributeProviderType.METHOD)
@LazyAttributeDomain("com.wrenched.example.domain.TreeNode")
public class TreeLoader {
	private final NodeType tree;
	
	public TreeLoader(String fileName) throws XmlException, IOException, InstantiationException {
		RootDocument document =
			RootDocument.Factory.parse(new DefaultResourceLoader().getResource(fileName).getInputStream());
		
		XmlOptions options = new XmlOptions();
		List<String> errors = new ArrayList<String>();
		options.put(XmlOptions.ERROR_LISTENER, errors);
		boolean isValid = document.validate(options);
		
        if (document != null && isValid) {
        	tree = fill(document.getRoot());
        }
        else {
        	throw new InstantiationException(errors.toString());
        }
	}

	private NodeType fill(NodeType node) {
		if (node.getChildren() != null) {
			for (NodeType child : node.getChildren().getNodeArray()) {
				child.setParent(node);
				fill(child);
			}
		}
		
		return node;
	}

	private NodeType find(Integer id, NodeType node) {
		NodeType result = null;

		if (Integer.parseInt(node.getId()) == id.intValue()) {
			result = node;
		}
		else if (node.getChildren() == null) {
			return null;
		}
		
		int i = 0;
		
		while (result == null && i < node.getChildren().getNodeArray().length) {
			result = find(id, node.getChildren().getNodeArray()[i++]);
		}
		
		return result;
	}
	
	@LazyAttributeFetcher(targetClass = TreeNode.class, idName = "id", attributeName = "parent")
	public TreeNode getParent(Integer id) {
		NodeType node = find(id, tree).getParent();
		
		return (node != null && !node.isNil()) ? new TreeNode(node) : null;
	}
	
	public TreeNode getNode(Integer id) {
		NodeType node = find(id, tree);
		return (node != null) ? new TreeNode(node) : null;
	}

	@LazyAttributeFetcher(targetClass = TreeNode.class, idName = "id", attributeName = "children")
	public List<TreeNode> getChildren(Integer id) {
		NodeType node = find(id, tree);
		
		if (node.getChildren() != null) {
			List<TreeNode> result = new ArrayList<TreeNode>();
			for (NodeType child : find(id, tree).getChildren().getNodeArray()) {
				result.add(new TreeNode(child));
			}
			
			return result;
		}
		else {
			return null;
		}
	}
}
