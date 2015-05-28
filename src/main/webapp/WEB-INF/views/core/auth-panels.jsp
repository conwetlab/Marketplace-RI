<div id="right-sidebar" class="panel panel-default-darker panel-sliding panel-sliding-right">
  <div class="panel-heading text-center">
    <span class="image-thumbnail">
      <span class="image image-circle image-primary-ligther">
        <span class="fa fa-user fa-inverse"></span>
      </span>
    </span>
    <span class="panel-title">${ user.displayName }</span>
    <span class="panel-subtitle">${ user.email }</span>
  </div>
  <div class="panel-body">
    <div class="list-group">
      <a class="list-group-item link-settings" href="${ pageContext.request.contextPath }/account">
        <span class="item-icon fa fa-cog"></span>
        <span class="item-text">Settings</span>
      </a>
      <a class="list-group-item link-logout" href="javascript:app.view.logout()">
        <span class="item-icon fa fa-sign-out"></span>
        <span class="item-text">Log out</span>
      </a>
    </div>
  </div>
</div>

<div id="left-sidebar" class="panel panel-default panel-sliding panel-sliding-left">
  <div class="panel-body">
    <div class="list-group">
      <div class="list-group-body">
        <a class="list-group-item" href="${ pageContext.request.contextPath }/offerings/bookmarks">
          <span class="item-icon fa fa-bookmark"></span>
          <span class="item-text">My bookmarks</span>
        </a>
      </div>
    </div>
    <div class="list-group">
      <div class="list-group-heading">DESCRIPTIONS</div>
      <div class="list-group-body">
        <a class="list-group-item link-list-descriptions" href="${ pageContext.request.contextPath }/descriptions">
          <span class="item-icon fa fa-archive"></span>
          <span class="item-text">My descriptions</span>
        </a>
        <a class="list-group-item link-create-description" href="${ pageContext.request.contextPath }/descriptions/register">
          <span class="item-icon fa fa-upload"></span>
          <span class="item-text">Upload a new description</span>
        </a>
      </div>
    </div>
    <div class="list-group">
      <div class="list-group-heading">STORES</div>
      <div class="list-group-body store-group"></div>
      <div class="list-group-body">
        <a class="list-group-item link-create-store" href="${ pageContext.request.contextPath }/stores/register">
          <span class="item-icon fa fa-plus-circle"></span>
          <span class="item-text">Register a new store</span>
        </a>
      </div>
    </div>
  </div>
</div>

<form name="logout_form" method="post" action="${ pageContext.request.contextPath }/logout"></form>