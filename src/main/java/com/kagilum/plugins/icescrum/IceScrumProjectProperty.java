package com.kagilum.plugins.icescrum;

import hudson.Extension;
import hudson.model.*;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import static org.apache.commons.lang.StringUtils.isEmpty;

public final class IceScrumProjectProperty extends JobProperty<AbstractProject<?, ?>> {

    private IceScrumProjectSettings settings;

    @DataBoundConstructor
    public IceScrumProjectProperty(String url, String username, String password) {
        if (username != null && password != null)
            this.settings = new IceScrumProjectSettings(url, username, password);
        else {
            this.settings = new IceScrumProjectSettings(url);
        }
    }

    public IceScrumProjectSettings getSettings() {
        return this.settings;
    }

    @Override
    public Collection<? extends Action> getJobActions(AbstractProject<?, ?> job) {
        if (settings != null) {
            return Collections.singleton(new IceScrumLinkAction(this));
        }
        return Collections.emptyList();
    }

    @Extension
    public static final class DescriptorImpl extends JobPropertyDescriptor {

        public DescriptorImpl() {
            super(IceScrumProjectProperty.class);
            load();
        }

        public boolean isApplicable(Class<? extends Job> jobType) {
            return AbstractProject.class.isAssignableFrom(jobType);
        }

        public String getDisplayName() {
            return Messages.IceScrumProjectProperty_icescrum_projectProperty_displayName();
        }

        public FormValidation doCheckUrl(@QueryParameter String value) {
            if(IceScrumProjectSettings.isValidUrl(value))
                return FormValidation.ok();
            else
                return FormValidation.error(Messages.IceScrumProjectProperty_icescrum_error_url());
        }

        public FormValidation doLoginCheck(@QueryParameter("icescrum.username") final String username,
                                           @QueryParameter("icescrum.password") final String password,
                                           @QueryParameter("icescrum.url") final String url) throws IOException, ServletException {

            if(!IceScrumProjectSettings.isValidUrl(url))
                return FormValidation.error(Messages.IceScrumProjectProperty_icescrum_error_url());

            if (isEmpty(username) || isEmpty(password) || isEmpty(url)){
                return FormValidation.error(Messages.IceScrumProjectProperty_icescrum_parameters_missing());
            } else {
                IceScrumProjectSettings settings = new IceScrumProjectSettings(url, username, password);
                IceScrumSession session = new IceScrumSession(settings);
                if(!session.isConnect()){
                    return FormValidation.ok(session.getLastError());
                }
            }
            return FormValidation.ok(Messages.IceScrumProjectProperty_icescrum_connection_successful());
        }

        @Override
        public JobProperty<?> newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            IceScrumProjectProperty ipp = req.bindJSON(IceScrumProjectProperty.class, formData);
            if (ipp.getSettings() == null) {
                ipp = null; // not configured
            }
            return ipp;
        }

    }
}