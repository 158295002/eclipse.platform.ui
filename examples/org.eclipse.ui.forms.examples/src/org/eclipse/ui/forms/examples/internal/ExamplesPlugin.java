package org.eclipse.ui.forms.examples.internal;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
/**
 * The main plugin class to be used in the desktop.
 */
public class ExamplesPlugin extends AbstractUIPlugin {
	//The shared instance.
	private static ExamplesPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
	private FormColors formColors;
	public static final String IMG_FORM_BG = "formBg";
	public static final String IMG_LARGE = "large";
	public static final String IMG_HORIZONTAL = "horizontal";
	public static final String IMG_VERTICAL = "vertical";
	public static final String IMG_SAMPLE = "sample";

	/**
	 * The constructor.
	 */
	public ExamplesPlugin() {
		plugin = this;
		try {
			resourceBundle = ResourceBundle
					.getBundle("org.eclipse.ui.forms.examples.internal.ExamplesPluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

	protected void initializeImageRegistry(ImageRegistry registry) {
		registerImage(registry, IMG_FORM_BG, "form_banner.gif");
		registerImage(registry, IMG_LARGE, "large_image.gif");
		registerImage(registry, IMG_HORIZONTAL, "th_horizontal.gif");
		registerImage(registry, IMG_VERTICAL, "th_vertical.gif");
		registerImage(registry, IMG_SAMPLE, "sample.gif");
	}

	private void registerImage(ImageRegistry registry, String key,
			String fileName) {
		try {
			IPath path = new Path("icons/" + fileName);
			URL url = find(path);
			if (url!=null) {
				ImageDescriptor desc = ImageDescriptor.createFromURL(url);
				registry.put(key, desc);
			}
		} catch (Exception e) {
		}
	}

	public FormColors getFormColors(Display display) {
		if (formColors == null) {
			formColors = new FormColors(display);
			formColors.markShared();
		}
		return formColors;
	}
	/**
	 * Returns the shared instance.
	 */
	public static ExamplesPlugin getDefault() {
		return plugin;
	}
	/**
	 * Returns the workspace instance.
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}
	/**
	 * Returns the string from the plugin's resource bundle, or 'key' if not
	 * found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = ExamplesPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null ? bundle.getString(key) : key);
		} catch (MissingResourceException e) {
			return key;
		}
	}
	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}
	public void stop(BundleContext context) throws Exception {
		try {
			if (formColors != null) {
				formColors.dispose();
				formColors = null;
			}
		} finally {
			super.stop(context);
		}
	}
	public Image getImage(String key) {
		return getImageRegistry().get(key);
	}
}