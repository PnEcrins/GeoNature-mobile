package com.geonature.mobile.commons.settings;

/**
 * Describes a task using {@link ServiceStatus}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
@SuppressWarnings("ALL")
@Deprecated
public interface IServiceMessageStatusTask {

    ServiceStatus getServiceStatus();
}
