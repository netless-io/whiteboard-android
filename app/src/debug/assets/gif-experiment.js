(function () {
  if (window.__gifExperiment) return;
  var mode = 'baseline', ids = new WeakMap(), serial = 0, originals = new Map(), slowOriginals = new Map();
  function emit(type, value) { __gifNative.event(JSON.stringify({type:type, time:Date.now(), mode:mode, value:value})); }
  function id(el) { if (!ids.has(el)) ids.set(el, ++serial); return ids.get(el); }
  function name(url) { try { return new URL(url, location.href).pathname.split('/').pop(); } catch (_) { return ''; } }
  function style(el) {
    var c = getComputedStyle(el), r = el.getBoundingClientRect();
    return {id:id(el), tag:el.tagName, cls:String(el.className), rect:[r.x,r.y,r.width,r.height],
      display:c.display, visibility:c.visibility, opacity:c.opacity, transform:c.transform,
      clipPath:c.clipPath, overflow:c.overflow, contain:c.contain, willChange:c.willChange,
      position:c.position, zIndex:c.zIndex, backfaceVisibility:c.backfaceVisibility};
  }
  function images() {
    return Array.from(document.querySelectorAll('.netless-app-presentation-content img')).map(function(img) {
      return Object.assign(style(img), {src:name(img.currentSrc || img.src), complete:img.complete,
        natural:[img.naturalWidth,img.naturalHeight], alt:img.alt,
        box:img.closest('.telebox-box') && img.closest('.telebox-box').className,
        parents:(function(){var a=[],p=img.parentElement;while(p&&a.length<8){a.push(style(p));p=p.parentElement;}return a;})()});
    });
  }
  var custom = document.createElement('style'); custom.id = 'gif-experiment-style'; document.head.appendChild(custom);
  function apply(next) {
    mode = next;
    document.querySelectorAll('style').forEach(function(s) {
      if (s !== custom && s.textContent.includes('.telebox-box.telebox-blur.telebox-maximized .netless-app-presentation-content img')) {
        if (!originals.has(s)) originals.set(s, s.textContent);
      }
    });
    originals.forEach(function(text, s){s.textContent=text;});
    if (next.indexOf('slow-') !== 0) {
      slowOriginals.forEach(function(src,img){ if (img.isConnected) img.src=src; });
      slowOriginals.clear();
    }
    custom.textContent = '';
    var root = '.netless-app-presentation-content ';
    if (next === 'remove-hide') originals.forEach(function(text,s) {
      s.textContent=text.replace(/\.telebox-box\.telebox-blur\.telebox-maximized \.netless-app-presentation-content img\s*\{[^}]*\}/g,'')
        .replace(/\.netless-app-presentation-content:not\(\.netless-app-presentation-preview-active\)\s*>\s*\.netless-app-presentation-preview\s*\{[^}]*\}/g,'');
    });
    if (next === 'promote-img') custom.textContent=root+'img[alt="background"]{transform:translateZ(0) !important;will-change:transform;}';
    if (next === 'promote-focused-img') custom.textContent='.telebox-box:not(.telebox-blur) '+root+'img[alt="background"]{transform:translateZ(0) !important;will-change:transform;}';
    if (next === 'will-change-only') custom.textContent='.telebox-box:not(.telebox-blur) '+root+'img[alt="background"]{will-change:transform;}';
    if (next === 'slow-source') {
      var target = Array.from(document.querySelectorAll(root+'img[alt="background"]')).find(function(i){return i.getBoundingClientRect().width>0 && !i.closest('.telebox-box').classList.contains('telebox-blur');});
      if (target) {
        if (!slowOriginals.has(target)) slowOriginals.set(target,target.src);
        target.src='http://127.0.0.1:18765/gif2.gif?run='+Date.now();
      }
    }
    if (next === 'slow-promote') custom.textContent=root+'img[alt="background"]{transform:translateZ(0) !important;will-change:transform;}';
    if (next === 'promote-background') custom.textContent=root+'.background{transform:translateZ(0) !important;will-change:transform;}';
    if (next === 'no-clip') custom.textContent=root+'[style*="clip-path"]{clip-path:none !important;}';
    emit('apply', {mode:mode, overwriteStyles:originals.size, css:custom.textContent});
    sample();
  }
  function sample() { emit('sample', {images:images(), visibility:document.visibilityState}); }
  ['load','error'].forEach(function(event){document.addEventListener(event,function(e){if(e.target.tagName==='IMG')emit(event,{id:id(e.target),src:name(e.target.src),complete:e.target.complete,natural:[e.target.naturalWidth,e.target.naturalHeight]});},true);});
  new MutationObserver(function(records){
    records.forEach(function(r){if(r.type==='attributes'&&(r.target.tagName==='IMG'||r.target.matches('.telebox-box,.netless-app-presentation-content')))
      emit('mutation',{id:id(r.target),attr:r.attributeName,value:r.attributeName==='src'?name(r.target.getAttribute('src')):r.target.getAttribute(r.attributeName)});});
  }).observe(document.documentElement,{subtree:true,attributes:true,attributeFilter:['src','style','class','hidden']});
  window.__gifExperiment = {sample:sample, apply:apply};
  emit('installed',{userAgent:navigator.userAgent});
})();
