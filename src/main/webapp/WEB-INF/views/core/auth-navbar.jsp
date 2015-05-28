<div class="navbar navbar-fixed-top">
  <div class="container">
    <div class="navbar-header">
      <a class="navbar-brand" href="${ pageContext.request.contextPath }">WMarket</a>
    </div>
    <div class="navbar-element pull-left">
      <button id="toggle-left-sidebar" class="btn btn-default" type="button">
        <span class="btn-icon fa fa-bars"></span>
      </button>
    </div>
    <div class="navbar-element pull-right">
      <button id="toggle-right-sidebar" class="btn btn-primary" type="button">
        <span class="btn-icon fa fa-user"></span>
        <span class="btn-text hidden-sm">${ user.displayName }</span>
      </button>
    </div>
  </div>
</div>