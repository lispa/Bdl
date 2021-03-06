package it.lispa.bdl.client.vc.ammgestindex;

import it.lispa.bdl.client.events.ChangeGridState;
import it.lispa.bdl.client.events.ChangeGridState.SaveHandler;
import it.lispa.bdl.client.place.NameTokens;
import it.lispa.bdl.client.ui.LayoutController;
import it.lispa.bdl.client.utils.GXTMessageBox;
import it.lispa.bdl.shared.dto.OggettoDTO;
import it.lispa.bdl.shared.dto.VOggettoDTO;
import it.lispa.bdl.shared.messages.AmmGestIndexMsg;
import it.lispa.bdl.shared.services.AmmGestIndexService;
import it.lispa.bdl.shared.services.AmmGestIndexServiceAsync;

import java.util.List;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.HideEvent;
import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.event.ShowEvent;
import com.sencha.gxt.widget.core.client.event.ShowEvent.ShowHandler;
import com.sencha.gxt.widget.core.client.grid.Grid;

//http://stackoverflow.com/questions/10694589/how-to-use-gwts-editor-framework-with-gwt-platform
/**
 * Class AmmGestIndexController.
 */
public class AmmGestIndexController extends Presenter<AmmGestIndexController.iAmmGestIndexView, AmmGestIndexController.iAmmGestIndexProxy> {

	@Inject
	AmmGestIndexMsg messages;

	AmmGestIndexServiceAsync servizio = (AmmGestIndexServiceAsync) GWT.create(AmmGestIndexService.class);

	private EventBus eventBus;

	/**
	 * Interface iAmmGestIndexView.
	 */
	public interface iAmmGestIndexView extends View {
		//public TextButton getInserisci();

		/**
		 * Legge visualizza.
		 *
		 * @return visualizza
		 */
		public TextButton getVisualizza();

		/**
		 * Legge cancella.
		 *
		 * @return cancella
		 */
		public TextButton getCancella();

		/**
		 * Load.
		 */
		public void load();

		/**
		 * Legge grid.
		 *
		 * @return grid
		 */
		public Grid<VOggettoDTO> getGrid();

		/**
		 * Legge panel.
		 *
		 * @return panel
		 */
		public ContentPanel getPanel();
	}

	/**
	 * Interface iAmmGestIndexProxy.
	 */
	@ProxyCodeSplit
	@NameToken(NameTokens.AmmGestIndex)
	public interface iAmmGestIndexProxy extends ProxyPlace<AmmGestIndexController> {
	}

	private AmmGestIndexFormController editorPresenter;
	private VOggettoDTO gridItem;

	/**
	 * Istanzia un nuovo amm gest index controller.
	 *
	 * @param eventBus  event bus
	 * @param view  view
	 * @param proxy  proxy
	 * @param editorPresenter  editor presenter
	 */
	@Inject
	public AmmGestIndexController(final EventBus eventBus, final iAmmGestIndexView view, final iAmmGestIndexProxy proxy,
			AmmGestIndexFormController editorPresenter) {

		super(eventBus, view, proxy);
		this.eventBus = eventBus;
		this.editorPresenter = editorPresenter;
	}

	private final SaveHandler saveHandler = new SaveHandler() {

		public void onSave(ChangeGridState event) {
			getView().load();
		}
	};

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this, LayoutController.SLOT_content, this);
	}

	@Override
	protected void onBind() {
		super.onBind();

		registerHandler(getEventBus().addHandler(ChangeGridState.getType(), saveHandler));

		editorPresenter.getView().getDialog().addHideHandler(new HideHandler() {
			public void onHide(HideEvent event) {
				getView().getPanel().unmask();
			}
		});
		editorPresenter.getView().getDialog().addShowHandler(new ShowHandler() {
			public void onShow(ShowEvent event) {
				getView().getPanel().mask();
			}
		});

		/*getView().getInserisci().addSelectHandler(new SelectHandler() {

			public void onSelect(SelectEvent event) {
				editorPresenter.activateInsertForm();
				addToPopupSlot(editorPresenter);
			}
		});*/

		getView().getVisualizza().addSelectHandler(new SelectHandler() {
			public void onSelect(SelectEvent event) {
				gridItem = getView().getGrid().getSelectionModel().getSelectedItem();
				servizio.getItemByCodice(gridItem.getO_CdOggettoOriginale(), new AsyncCallback<OggettoDTO>() {
					public void onFailure(Throwable caught) {
						// Non fare nulla: l'errore generico viene gestito a monte...
					}

					@Override
					public void onSuccess(OggettoDTO item) {
						editorPresenter.activateView(item);
						addToPopupSlot(editorPresenter);
					}
				});
			}
		});

		getView().getCancella().addSelectHandler(new SelectHandler() {

			public void onSelect(SelectEvent event) {
				final List<VOggettoDTO> gridItems = getView().getGrid().getSelectionModel().getSelectedItems();

				String message;
				if (gridItems.size() == 1) {
					message = messages.lMsgCancellaConfirm(gridItems.get(0).getO_DnTitolo());
				} else {
					message = messages.lMsgCancellaAllConfirm(gridItems.size());
				}

				ConfirmMessageBox box = new ConfirmMessageBox(messages.titlePanel(), message);
				box.setSize("400", "100");
				box.addHideHandler(new HideHandler() {

					public void onHide(HideEvent event) {
						Dialog btn = (Dialog) event.getSource();
						if (btn.getHideButton() == btn.getButtonById(PredefinedButton.YES.name())) {
							servizio.cancellaItems(gridItems, new AsyncCallback<Void>() {
								public void onFailure(Throwable caught) {
									// Non fare nulla: l'errore generico viene
									// gestito a monte...
									// e il metodo non tira un
									// AsyncServiceException
								}

								@Override
								public void onSuccess(Void v) {
									new GXTMessageBox(messages.titlePanel(), messages.lMsgCancellaEsito(), GXTMessageBox.DO_SHOW);
									eventBus.fireEvent(new ChangeGridState());
								}
							});

						} else if (btn.getHideButton() == btn.getButtonById(PredefinedButton.NO.name())) {
							// perform NO action
						}
					}
				});
				box.show();
			}
		});
	}

}
