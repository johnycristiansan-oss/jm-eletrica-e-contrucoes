const JM={state:JSON.parse(localStorage.getItem('jm_state')||'{}'),save(){localStorage.setItem('jm_state',JSON.stringify(this.state));if(window.JMNative)JMNative.saveState(JSON.stringify(this.state))},load(){try{const n=window.JMNative&&JMNative.loadState();if(n&&n!=='{}')this.state={...this.state,...JSON.parse(n)}}catch(e){}localStorage.setItem('jm_state',JSON.stringify(this.state))},logged(){return !!this.state.account},draft(d){this.state.draft={...(this.state.draft||{}),...d};this.save()},clearDraft(){delete this.state.draft;this.save()}};
JM.load();
window.onNativeReady=()=>JM.load();
window.onNativeLocation=(lat,lon)=>{JM.draft({location:{lat,lon,accuracy:'native'}});if(window.onLocationReady)window.onLocationReady(lat,lon)};
window.onNativeLocationUnavailable=()=>window.onLocationUnavailable&&window.onLocationUnavailable();
window.onLocationPermissionResult=ok=>window.onLocationPermissionResult&&window.onLocationPermissionResult(ok);
function requestAppLocation(){if(window.JMNative)JMNative.requestLocation();else alert('Localização disponível quando executado no Android.');}
function requireLogin(action){if(JM.logged())return action();show('auth');document.getElementById('authMessage').textContent='Entre ou crie sua conta para continuar. Seu orçamento já está salvo.';window.afterAuth=action}
function fakeLogin(provider){JM.state.account={provider,name:provider==='google'?'Cliente Google':'Cliente'};JM.save();show('home');if(window.afterAuth){const a=window.afterAuth;window.afterAuth=null;a()}}
function saveDraft(){JM.draft({updatedAt:Date.now()})}
function saveQuote(){JM.draft({status:'ENVIADO',verification:'ESTIMATE'});show('sent')}
function viewPrices(){requireLogin(()=>show('prices'))}
function addTravelCost(){const v=prompt('Valor de deslocamento que o profissional normalmente cobra (R$):','0');const n=Math.max(0,Number(String(v).replace(',','.'))||0);JM.draft({travelCost:n});saveDraft()}
function confirmProfessionalCheck(){JM.draft({verification:'PROFESSIONAL_CHECKED',checkedAt:Date.now()});renderReview()}
function renderReview(){if(typeof q==='undefined')return;const d=JM.state.draft||{};q.innerHTML='<div class="summary"><h2 class="q">Revise seu pedido</h2><div class="sumrow"><span>Serviço</span><b>'+esc(d.service||'Não informado')+'</b></div><div class="sumrow"><span>Local</span><b>'+esc(d.property||'Não informado')+'</b></div><div class="sumrow"><span>Medidas</span><b>'+esc(d.measurements||'Não informadas')+'</b></div><div class="sumrow"><span>Fotos</span><b>'+((d.photos||[]).length)+' foto(s)</b></div><div class="notice"><b>Estimativa:</b> medidas e quantidades são informadas pelo cliente e precisam ser conferidas pelo profissional.</div><button class="primary" onclick="saveQuote()">Enviar pedido</button></div>'}
function esc(v){return String(v).replace(/[&<>\"']/g,m=>({'&':'&amp;','<':'&lt;','>':'&gt;','\"':'&quot;',"'":'&#39;'}[m]))}
