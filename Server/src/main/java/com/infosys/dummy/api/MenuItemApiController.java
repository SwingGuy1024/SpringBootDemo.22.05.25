package com.infosys.dummy.api;

import com.infosys.dummy.engine.DataEngine;
import com.infosys.dummy.framework.ResponseUtility;
import com.infosys.dummy.model.MenuItemDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.List;
import java.util.Optional;

//import static com.infosys.dummy.framework.ResponseUtility.logHeaders;

@Controller
@RequestMapping("${openapi.customerOrders.base-path:}")
public class MenuItemApiController implements MenuItemApi {
  private static final Logger log = LoggerFactory.getLogger(MenuItemApiController.class);

  private final NativeWebRequest request;

  private final DataEngine dataEngine;
  
  @Autowired
  public MenuItemApiController(
      @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
      NativeWebRequest request,
      DataEngine dataEngine
  ) {
    this.request = request;
    this.dataEngine = dataEngine;
  }

  @Override
  public Optional<NativeWebRequest> getRequest() {
    return Optional.ofNullable(request);
  }

  @Override
  public ResponseEntity<MenuItemDto> getMenuItem(final Integer id) {
//    logHeaders(request, "MenuItemApiController.getMenuItem(id)");
    return ResponseUtility.serveOK(() -> dataEngine.getMenuItemDto(id));
  }

  @Override
  public ResponseEntity<List<MenuItemDto>> getAll() {
//    logHeaders(request, "MenuItemApiController.getAll()");
    return ResponseUtility.serveOK(dataEngine::getAllMenuItems);
  }
}
