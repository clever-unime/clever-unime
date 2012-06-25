/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clever.HostManager.NetworkManager;

/**
 *
 * @author Patrizio
 */
public class PluginDescription
{
	private String name;
	private String version;
	private String shortDescription;
	private String technology;
	private String techVersion;

	public PluginDescription(String name, String version, String shorDescription, String technology, String techVersion)
	{
		setName(name);
		setVersion(version);
		setShortDescription(shortDescription);
		setTechnology(technology);
		setTechVersion(techVersion);
	}

	private void setName(String name) {
		this.name=name;
	}

	private void setVersion(String version) {
		this.version=version;
	}

	private void setShortDescription(String shortDescription) {
		this.shortDescription=shortDescription;
	}

	private void setTechVersion(String techVersion) {
		this.techVersion=techVersion;
	}

	private void setTechnology(String technology) {
		this.technology=technology;
	}
	

	public String getName()
	{
		return this.name;
	}
	public String getVersion()
	{
		return this.version;
	}
	public String getShortDescription()
	{
		return this.shortDescription;
	}
	public String getTechVersion()
	{
		return this.technology;
	}
	public String getTechnology()
	{
		return this.techVersion;
	}


}
