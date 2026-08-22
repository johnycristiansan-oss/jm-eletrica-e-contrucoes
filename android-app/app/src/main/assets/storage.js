/* Persistência local v1 — preparada para futura sincronização com backend. */
(function(){
  const PREFIX='jm_app_v1_';
  function read(key,fallback){try{const v=localStorage.getItem(PREFIX+key);return v===null?fallback:JSON.parse(v)}catch(e){return fallback}}
  function write(key,value){localStorage.setItem(PREFIX+key,JSON.stringify(value));return value}
  window.JMStorage={
    getProfile:()=>read('profile',{}),
    saveProfile:p=>write('profile',p||{}),
    getLocation:()=>read('location',{}),
    saveLocation:l=>write('location',l||{}),
    getRequests:()=>read('requests',[]),
    saveRequests:r=>write('requests',Array.isArray(r)?r:[]),
    addRequest:r=>{const list=read('requests',[]);list.push(r);return write('requests',list)},
    getDraft:()=>read('draft',null),
    saveDraft:d=>write('draft',d),
    clearDraft:()=>localStorage.removeItem(PREFIX+'draft'),
    clearAll:()=>Object.keys(localStorage).filter(k=>k.startsWith(PREFIX)).forEach(k=>localStorage.removeItem(k))
  };
})();
