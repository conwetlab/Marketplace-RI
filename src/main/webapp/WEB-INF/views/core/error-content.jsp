<div class="container">
  <div class="col-sm-10">
    <div class="alert alert-${ statusCode < 500 ? 'warning' : 'danger' }">
      <div class="alert-heading">Error ${ statusCode } - ${ reasonPhrase }</div>
      <div class="alert-content">${ content }</div>
    </div>
  </div>
</div>