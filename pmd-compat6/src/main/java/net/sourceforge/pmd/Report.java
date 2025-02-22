/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

// This class has been taken from 7.0.0-SNAPSHOT
// Changes: filterViolations(net.sourceforge.pmd.util.Predicate<RuleViolation> filter)

package net.sourceforge.pmd;

import static java.util.Collections.synchronizedList;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.sourceforge.pmd.annotation.DeprecatedUntil700;
import net.sourceforge.pmd.annotation.Experimental;
import net.sourceforge.pmd.annotation.InternalApi;
import net.sourceforge.pmd.lang.document.FileId;
import net.sourceforge.pmd.lang.document.TextFile;
import net.sourceforge.pmd.renderers.AbstractAccumulatingRenderer;
import net.sourceforge.pmd.reporting.FileAnalysisListener;
import net.sourceforge.pmd.reporting.GlobalAnalysisListener;
import net.sourceforge.pmd.util.BaseResultProducingCloseable;

/**
 * A {@link Report} collects all informations during a PMD execution. This
 * includes violations, suppressed violations, metrics, error during processing
 * and configuration errors.
 *
 * <p>A report may be created by a {@link GlobalReportBuilderListener} that you
 * use as the {@linkplain GlobalAnalysisListener} in {@link PmdAnalysis#performAnalysisAndCollectReport() PMD's entry point}.
 * You can also create one manually with {@link #buildReport(Consumer)}.
 *
 * <p>For special use cases, like filtering the report after PMD analysis and
 * before rendering the report, some transformation operations are provided:
 * <ul>
 *     <li>{@link #filterViolations(Predicate)}</li>
 *     <li>{@link #union(Report)}</li>
 * </ul>
 * These methods create a new {@link Report} rather than modifying their receiver.
 * </p>
 */
public final class Report {
    // todo move to package reporting

    private final List<RuleViolation> violations = synchronizedList(new ArrayList<>());
    private final List<SuppressedViolation> suppressedRuleViolations = synchronizedList(new ArrayList<>());
    private final List<ProcessingError> errors = synchronizedList(new ArrayList<>());
    private final List<ConfigurationError> configErrors = synchronizedList(new ArrayList<>());

    @DeprecatedUntil700
    @InternalApi
    public Report() { // NOPMD - UnnecessaryConstructor
        // TODO: should be package-private, you have to use a listener to build a report.
    }

    /**
     * Represents a configuration error.
     */
    public static class ConfigurationError {

        private final Rule rule;
        private final String issue;

        /**
         * Creates a new configuration error for a specific rule.
         *
         * @param theRule
         *            the rule which is configured wrongly
         * @param theIssue
         *            the reason, why the configuration is wrong
         */
        public ConfigurationError(Rule theRule, String theIssue) {
            rule = theRule;
            issue = theIssue;
        }

        /**
         * Gets the wrongly configured rule
         *
         * @return the wrongly configured rule
         */
        public Rule rule() {
            return rule;
        }

        /**
         * Gets the reason for the configuration error.
         *
         * @return the issue
         */
        public String issue() {
            return issue;
        }
    }

    /**
     * Represents a processing error, such as a parse error.
     */
    public static class ProcessingError {

        private final Throwable error;
        private final FileId file;

        /**
         * Creates a new processing error
         *
         * @param error
         *            the error
         * @param file
         *            the file during which the error occurred
         */
        public ProcessingError(Throwable error, FileId file) {
            this.error = error;
            this.file = file;
        }

        public String getMsg() {
            return error.getClass().getSimpleName() + ": " + error.getMessage();
        }

        public String getDetail() {
            try (StringWriter stringWriter = new StringWriter();
                 PrintWriter writer = new PrintWriter(stringWriter)) {
                error.printStackTrace(writer);
                return stringWriter.toString();
            } catch (IOException e) {
                // IOException on close - should never happen when using StringWriter
                throw new RuntimeException(e);
            }
        }

        public FileId getFileId() {
            return file;
        }

        public Throwable getError() {
            return error;
        }

        // ------------------- compat extensions --------------------
        public String getFile() {
            return file.getAbsolutePath();
        }
    }

    /**
     * Represents a violation, that has been suppressed.
     */
    public static class SuppressedViolation {

        private final RuleViolation rv;
        private final String userMessage;
        private final ViolationSuppressor suppressor;

        /**
         * Creates a suppressed violation.
         *
         * @param rv          The violation, that has been suppressed
         * @param suppressor  The suppressor which suppressed the violation
         * @param userMessage Any relevant info given by the suppressor
         */
        public SuppressedViolation(RuleViolation rv, ViolationSuppressor suppressor, String userMessage) {
            this.suppressor = suppressor;
            this.rv = rv;
            this.userMessage = userMessage;
        }

        public ViolationSuppressor getSuppressor() {
            return suppressor;
        }

        public RuleViolation getRuleViolation() {
            return this.rv;
        }

        public String getUserMessage() {
            return userMessage;
        }
    }

    /**
     * Adds a new rule violation to the report and notify the listeners.
     *
     * @param violation the violation to add
     *
     * @deprecated PMD's way of creating a report is internal and may be changed in pmd 7.
     */
    @DeprecatedUntil700
    @Deprecated
    @InternalApi
    public void addRuleViolation(RuleViolation violation) {
        synchronized (violations) {
            // note that this binary search is inefficient as we usually
            // report violations file by file.
            int index = Collections.binarySearch(violations, violation, RuleViolation.DEFAULT_COMPARATOR);
            violations.add(index < 0 ? -index - 1 : index, violation);
        }
    }

    /**
     * Adds a new suppressed violation.
     */
    private void addSuppressedViolation(SuppressedViolation sv) {
        suppressedRuleViolations.add(sv);
    }

    /**
     * Adds a new configuration error to the report.
     *
     * @param error the error to add
     *
     * @deprecated PMD's way of creating a report is internal and may be changed in pmd 7.
     */
    @DeprecatedUntil700
    @Deprecated
    @InternalApi
    public void addConfigError(ConfigurationError error) {
        configErrors.add(error);
    }

    /**
     * Adds a new processing error to the report.
     *
     * @param error
     *            the error to add
     * @deprecated PMD's way of creating a report is internal and may be changed in pmd 7.
     */
    @DeprecatedUntil700
    @Deprecated
    @InternalApi
    public void addError(ProcessingError error) {
        errors.add(error);
    }

    /**
     * Merges the given report into this report. This might be necessary, if a
     * summary over all violations is needed as PMD creates one report per file
     * by default.
     *
     * <p>This is synchronized on an internal lock (note that other mutation
     * operations are not synchronized, todo for pmd 7).
     *
     * @param r the report to be merged into this.
     *
     * @see AbstractAccumulatingRenderer
     *
     * @deprecated Convert Renderer to use the reports.
     */
    @Deprecated
    public void merge(Report r) {
        errors.addAll(r.errors);
        configErrors.addAll(r.configErrors);
        suppressedRuleViolations.addAll(r.suppressedRuleViolations);

        for (RuleViolation violation : r.getViolations()) {
            addRuleViolation(violation);
        }
    }


    /**
     * Returns an unmodifiable list of violations that were suppressed.
     */
    public List<SuppressedViolation> getSuppressedViolations() {
        return Collections.unmodifiableList(suppressedRuleViolations);
    }

    /**
     * Returns an unmodifiable list of violations that have been
     * recorded until now. None of those violations were suppressed.
     *
     * <p>The violations list is sorted with {@link RuleViolation#DEFAULT_COMPARATOR}.
     */
    public List<RuleViolation> getViolations() {
        return Collections.unmodifiableList(violations);
    }


    /**
     * Returns an unmodifiable list of processing errors that have been
     * recorded until now.
     */
    public List<ProcessingError> getProcessingErrors() {
        return Collections.unmodifiableList(errors);
    }


    /**
     * Returns an unmodifiable list of configuration errors that have
     * been recorded until now.
     */
    public List<ConfigurationError> getConfigurationErrors() {
        return Collections.unmodifiableList(configErrors);
    }

    /**
     * Create a report by making side effects on a {@link FileAnalysisListener}.
     * This wraps a {@link ReportBuilderListener}.
     */
    public static Report buildReport(Consumer<? super FileAnalysisListener> lambda) {
        return BaseResultProducingCloseable.using(new ReportBuilderListener(), lambda);
    }

    /**
     * A {@link FileAnalysisListener} that accumulates events into a
     * {@link Report}.
     */
    public static final class ReportBuilderListener extends BaseResultProducingCloseable<Report> implements FileAnalysisListener {

        private final Report report;

        public ReportBuilderListener() {
            this(new Report());
        }

        ReportBuilderListener(Report report) {
            this.report = report;
        }

        @Override
        protected Report getResultImpl() {
            return report;
        }

        @Override
        public void onRuleViolation(RuleViolation violation) {
            report.addRuleViolation(violation);
        }

        @Override
        public void onSuppressedRuleViolation(SuppressedViolation violation) {
            report.addSuppressedViolation(violation);
        }

        @Override
        public void onError(ProcessingError error) {
            report.addError(error);
        }

        @Override
        public String toString() {
            return "ReportBuilderListener";
        }
    }

    /**
     * A {@link GlobalAnalysisListener} that accumulates the events of
     * all files into a {@link Report}.
     */
    public static final class GlobalReportBuilderListener extends BaseResultProducingCloseable<Report> implements GlobalAnalysisListener {

        private final Report report = new Report();

        @Override
        public FileAnalysisListener startFileAnalysis(TextFile file) {
            // note that the report is shared, but Report is now thread-safe
            return new ReportBuilderListener(this.report);
        }

        @Override
        public void onConfigError(ConfigurationError error) {
            report.addConfigError(error);
        }

        @Override
        protected Report getResultImpl() {
            return report;
        }
    }

    /**
     * Creates a new report taking all the information from this report,
     * but filtering the violations.
     *
     * @param filter when true, the violation will be kept.
     * @return copy of this report
     */
    @Experimental
    public Report filterViolations(Predicate<RuleViolation> filter) {
        Report copy = new Report();

        for (RuleViolation violation : violations) {
            if (filter.test(violation)) {
                copy.addRuleViolation(violation);
            }
        }

        copy.suppressedRuleViolations.addAll(suppressedRuleViolations);
        copy.errors.addAll(errors);
        copy.configErrors.addAll(configErrors);
        return copy;
    }

    /**
     * Creates a new report by combining this report with another report.
     * This is similar to {@link #merge(Report)}, but instead a new report
     * is created. The lowest start time and greatest end time are kept in the copy.
     *
     * @param other the other report to combine
     * @return
     */
    @Experimental
    public Report union(Report other) {
        Report copy = new Report();

        for (RuleViolation violation : violations) {
            copy.addRuleViolation(violation);
        }
        for (RuleViolation violation : other.violations) {
            copy.addRuleViolation(violation);
        }

        copy.suppressedRuleViolations.addAll(suppressedRuleViolations);
        copy.suppressedRuleViolations.addAll(other.suppressedRuleViolations);

        copy.errors.addAll(errors);
        copy.errors.addAll(other.errors);
        copy.configErrors.addAll(configErrors);
        copy.configErrors.addAll(other.configErrors);

        return copy;
    }

    // ------------------- compat extensions --------------------
    @Deprecated
    public Report filterViolations(net.sourceforge.pmd.util.Predicate<RuleViolation> filter) {
        Predicate<RuleViolation> javaPredicate = filter::test;
        return filterViolations(javaPredicate);
    }
}
