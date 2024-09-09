package com.infosys.dummy.api;

import com.infosys.dummy.framework.engine.DataEngine;
import com.infosys.dummy.model.MenuItemDto;
import com.infosys.dummy.model.MenuItemOptionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

import javax.validation.Valid;
import java.util.Optional;

import static com.infosys.dummy.framework.ResponseUtility.serveCreatedEntity;
import static com.infosys.dummy.framework.ResponseUtility.serveEmptyOK;

/**
 * <p>Controller to implement the generated AdminApi interface.</p>
 */
@RestController
@RequestMapping("${openapi.customerOrders.base-path:}")
public class AdminApiController implements AdminApi {

  private static final Logger log = LoggerFactory.getLogger(AdminApiController.class);

  private final NativeWebRequest request;

  private final DataEngine dataEngine;

  @Autowired
  public AdminApiController(
      final DataEngine dataEngine,
      @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") final NativeWebRequest request
  ) {
    this.request = request;
    this.dataEngine = dataEngine;
    log.trace("instantiating AdminApiController");
  }

  // I would never wrap this in an Optional, because it's never null, but the interface is generated.
  @Override
  public Optional<NativeWebRequest> getRequest() {
    return Optional.ofNullable(request);
  }

  @Override
  public ResponseEntity<String> addMenuItemOption(final Integer menuItemId, final MenuItemOptionDto optionDto) {
    log.trace("addMenuItemOption() to id {}: {}", menuItemId, optionDto);
    return serveCreatedEntity(() -> dataEngine.addOption(menuItemId, optionDto));
  }

  @Override
  public ResponseEntity<String> addMenuItem(final MenuItemDto menuItemDto) {
    log.trace("addMenuItem: {}", menuItemDto);
    return serveCreatedEntity(() -> dataEngine.addMenuItemFromDto(menuItemDto));
  }

  @Override
  public ResponseEntity<String> addNewMenuItemOption(@Valid final MenuItemOptionDto menuItemOptionDto) {
    log.trace("addNewMenuItemOption(): {}", menuItemOptionDto);
    return serveCreatedEntity(() -> dataEngine.createNewOption(menuItemOptionDto));
  }

  @Override
  public ResponseEntity<String> deleteOption(final Integer optionId) {
    return serveEmptyOK(() -> dataEngine.deleteOptionById(optionId));
  }

  @Override
  public ResponseEntity<String> addOptionToMenuItem(Integer menuId, Integer optionId) {
    return serveEmptyOK(() -> dataEngine.addOptionToItem(optionId, menuId));
  }
}
