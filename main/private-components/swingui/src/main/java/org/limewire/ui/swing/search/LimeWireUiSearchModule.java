package org.limewire.ui.swing.search;

import org.limewire.collection.AutoCompleteDictionary;
import org.limewire.collection.StringTrieSet;
import org.limewire.inject.FactoryModules;
import org.limewire.inject.LazyBinder;
import org.limewire.ui.swing.filter.AdvancedFilterPanelFactory;
import org.limewire.ui.swing.search.model.SimilarResultsDetectorFactory;
import org.limewire.ui.swing.search.model.SimilarResultsDetectorFactoryImpl;
import org.limewire.ui.swing.search.model.VisualSearchResult;
import org.limewire.ui.swing.search.model.VisualSearchResultFactory;
import org.limewire.ui.swing.search.model.VisualSearchResultFactoryImpl;
import org.limewire.ui.swing.search.resultpanel.BaseResultPanel;
import org.limewire.ui.swing.search.resultpanel.BaseResultPanelFactory;
import org.limewire.ui.swing.search.resultpanel.SearchHeadingDocumentBuilder;
import org.limewire.ui.swing.search.resultpanel.SearchHeadingDocumentBuilderImpl;
import org.limewire.ui.swing.search.resultpanel.SearchResultTruncator;
import org.limewire.ui.swing.search.resultpanel.SearchResultTruncatorImpl;
import org.limewire.ui.swing.search.resultpanel.list.ListViewRowHeightRule;
import org.limewire.ui.swing.search.resultpanel.list.ListViewRowHeightRuleImpl;
import org.limewire.ui.swing.search.resultpanel.list.ListViewTableEditorRenderer;
import org.limewire.ui.swing.search.resultpanel.list.ListViewTableEditorRendererFactory;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

/**
 * Module to configure Guice bindings for the UI search classes.
 */
public class LimeWireUiSearchModule extends AbstractModule {
    
    /**
     * Configures the bindings for the UI search classes.
     */
    @Override
    protected void configure() {
        bind(AutoCompleteDictionary.class).annotatedWith(Names.named("searchHistory")).toInstance(new StringTrieSet(true));
        install(FactoryModules.newFactory(SmartAutoCompleteFactory.class, SmartAutoCompleteDictionary.class));
        
        bind(SearchHandler.class).to(SearchHandlerImpl.class);
        bind(SearchHandler.class).annotatedWith(Names.named("text")).to(TextSearchHandlerImpl.class);
        bind(SimilarResultsDetectorFactory.class).to(SimilarResultsDetectorFactoryImpl.class);
        bind(VisualSearchResultFactory.class).to(VisualSearchResultFactoryImpl.class);
        
        install(FactoryModules.newFactory(SearchResultsPanelFactory.class, SearchResultsPanel.class));
        
        install(FactoryModules.newFactory(ResultsContainerFactory.class, ResultsContainer.class));
        
        install(FactoryModules.newFactory(SortAndFilterPanelFactory.class, SortAndFilterPanel.class));
        
        bind(new TypeLiteral<AdvancedFilterPanelFactory<VisualSearchResult>>(){}).to(
                SearchFilterPanelFactory.class);
        
        install(FactoryModules.newFactory(BaseResultPanelFactory.class, BaseResultPanel.class));
        
        install(FactoryModules.newFactory(ListViewTableEditorRendererFactory.class, ListViewTableEditorRenderer.class));       
        
        bind(FriendPresenceActions.class).toProvider(LazyBinder.newLazyProvider(
                FriendPresenceActions.class, FriendPresenceActionsImpl.class));
        
        bind(SearchHeadingDocumentBuilder.class).toProvider(LazyBinder.newLazyProvider(
                SearchHeadingDocumentBuilder.class, SearchHeadingDocumentBuilderImpl.class));
        
        bind(ListViewRowHeightRule.class).to(ListViewRowHeightRuleImpl.class);
        bind(SearchResultTruncator.class).toProvider(LazyBinder.newLazyProvider(
                SearchResultTruncator.class, SearchResultTruncatorImpl.class));
        
        install(FactoryModules.newFactory(BrowseFailedMessagePanelFactory.class, BrowseFailedMessagePanel.class));
    }
}
