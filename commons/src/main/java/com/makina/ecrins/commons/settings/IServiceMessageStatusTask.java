package com.makina.ecrins.commons.settings;

/**
 * Describes a task using {@link ServiceStatus}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
@Deprecated
public interface IServiceMessageStatusTask {

    ServiceStatus getServiceStatus();
}
