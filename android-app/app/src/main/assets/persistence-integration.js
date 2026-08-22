/* Integração v1 da persistência local. Carregado após storage.js. */
(function(){
  if(!window.JMStorage) return;
  const S=window.JMStorage;
  window.JMAppPersistence={
    restoreProfile(){return S.getProfile()},
    saveProfile(profile){return S.saveProfile(profile)},
    restoreLocation(){return S.getLocation()},
    saveLocation(location){return S.saveLocation(location)},
    restoreDraft(){return S.getDraft()},
    saveDraft(draft){return S.saveDraft(draft)},
    clearDraft(){return S.clearDraft()},
    listRequests(){return S.getRequests()},
    saveRequest(request){
      const item=Object.assign({id:'req_'+Date.now(),createdAt:new Date().toISOString(),status:'Rascunho'},request||{});
      S.addRequest(item);
      return item;
    },
    clearLocalData(){S.clearAll()}
  };
})();
