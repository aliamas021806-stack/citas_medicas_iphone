/* ============================================================
   CitasMedicas — Demo Web Interactiva
   Réplica funcional del app iOS (SwiftUI + MVVM).
   Datos y lógica: espejo de MockData.swift / repositorios Swift.
   Persistencia: localStorage (citas + sesión).
   ============================================================ */

/* ---------- 1. DATOS (MockData.swift) ---------- */
const SPECIALTIES = [
  {id:"esp_01",name:"Cardiología",emoji:"🫀",desc:"Enfermedades del corazón",count:3},
  {id:"esp_02",name:"Pediatría",emoji:"👶",desc:"Atención a niños",count:2},
  {id:"esp_03",name:"Dermatología",emoji:"🧴",desc:"Piel, cabello y uñas",count:2},
  {id:"esp_04",name:"Traumatología",emoji:"🦴",desc:"Sistema músculo-esquelético",count:2},
  {id:"esp_05",name:"Neurología",emoji:"🧠",desc:"Sistema nervioso",count:2},
  {id:"esp_06",name:"Medicina General",emoji:"🩺",desc:"Atención primaria",count:3},
  {id:"esp_07",name:"Ginecología",emoji:"🌸",desc:"Salud femenina",count:2},
  {id:"esp_08",name:"Oftalmología",emoji:"👁️",desc:"Visión y ojos",count:2}
];

const DOCTORS = [
  {id:"doc_01",fullName:"Carlos Ramírez",sp:"esp_01",spName:"Cardiología",bio:"Cardiólogo intervencionista con amplia experiencia en arritmias y prevención cardiovascular.",rating:4.8,years:15,age:52,fee:45,hospital:"Hospital Central Vitalis"},
  {id:"doc_02",fullName:"María González",sp:"esp_02",spName:"Pediatría",bio:"Pediatra dedicada al desarrollo infantil y vacunación. Paciente y cercana con los más pequeños.",rating:4.9,years:12,age:44,fee:35,hospital:"Clínica Infantil Aurora"},
  {id:"doc_03",fullName:"Andrea López",sp:"esp_03",spName:"Dermatología",bio:"Especialista en dermatología clínica y estética, tratamiento del acné y cuidado del melanoma.",rating:4.7,years:10,age:39,fee:40,hospital:"Centro DermaSalud"},
  {id:"doc_04",fullName:"Javier Moreno",sp:"esp_04",spName:"Traumatología",bio:"Traumatólogo especializado en lesiones deportivas y cirugía de rodilla.",rating:4.6,years:14,age:47,fee:50,hospital:"Hospital Central Vitalis"},
  {id:"doc_05",fullName:"Lucía Fernández",sp:"esp_05",spName:"Neurología",bio:"Neuróloga con enfoque en epilepsia, cefaleas y trastornos del movimiento.",rating:4.9,years:18,age:55,fee:55,hospital:"Instituto NeuroVida"},
  {id:"doc_06",fullName:"Diego Torres",sp:"esp_06",spName:"Medicina General",bio:"Médico general orientado a la prevención y seguimiento de enfermedades crónicas.",rating:4.5,years:8,age:36,fee:25,hospital:"Centro Médico Familiar"},
  {id:"doc_07",fullName:"Sofía Herrera",sp:"esp_07",spName:"Ginecología",bio:"Ginecóloga y obstetra, control prenatal y salud reproductiva de la mujer.",rating:4.8,years:13,age:45,fee:42,hospital:"Clínica Mujer & Vida"},
  {id:"doc_08",fullName:"Pablo Castro",sp:"esp_08",spName:"Oftalmología",bio:"Oftalmólogo especializado en cataratas, glaucoma y cirugía refractiva láser.",rating:4.7,years:16,age:49,fee:48,hospital:"Instituto Ocular Visio"},
  {id:"doc_09",fullName:"Elena Ruiz",sp:"esp_01",spName:"Cardiología",bio:"Cardióloga clínica enfocada en hipertensión y rehabilitación cardíaca.",rating:4.6,years:9,age:38,fee:43,hospital:"Hospital del Corazón"},
  {id:"doc_10",fullName:"Miguel Ángel Díaz",sp:"esp_06",spName:"Medicina General",bio:"Medicina general y urgencias, atención rápida y diagnóstico oportuno.",rating:4.4,years:6,age:34,fee:22,hospital:"Centro Médico Familiar"}
];

const PATIENTS = [
  {id:"pat_01",fullName:"Laura Jiménez",age:34,blood:"O+",gender:"Femenino",insurance:"SaludPlus",allergies:"Penicilina",chronic:"Ninguna"},
  {id:"pat_02",fullName:"Andrés Molina",age:47,blood:"A+",gender:"Masculino",insurance:"VidaTotal",allergies:"Ninguna",chronic:"Hipertensión"}
];

const HOURS = ["08:00","09:00","10:00","11:00","12:00","15:00","16:00","17:00","18:00"];
const WEEKDAYS = ["dom","lun","mar","mié","jue","vie","sáb"];
const MONTHS = ["ene","feb","mar","abr","may","jun","jul","ago","sep","oct","nov","dic"];

/* ---------- 2. "REPOSITORIOS" + PERSISTENCIA ---------- */
const DB = {
  userKey:"cm_user",
  apptKey:"cm_appointments",

  getUser(){ try{return JSON.parse(localStorage.getItem(this.userKey));}catch(e){return null;} },
  setUser(u){ localStorage.setItem(this.userKey, JSON.stringify(u)); },
  clearUser(){ localStorage.removeItem(this.userKey); },

  loadAppointments(){
    let raw = localStorage.getItem(this.apptKey);
    if(raw){ try{return JSON.parse(raw);}catch(e){} }
    // Sembrado inicial (equivale al SEED_CALLBACK de Room)
    const ds = selectableDates().map(fmtISO);
    const seed = [
      {id:"seed1",doctorId:"doc_01",doctorName:"Dr. Carlos Ramírez",specialtyName:"Cardiología",patientName:"Laura Jiménez",patientAge:34,date:ds[1]||ds[0],time:"10:00",status:"Confirmed",reason:"Chequeo cardiológico anual",modality:"Presencial"},
      {id:"seed2",doctorId:"doc_02",doctorName:"Dra. María González",specialtyName:"Pediatría",patientName:"Laura Jiménez",patientAge:34,date:ds[3]||ds[0],time:"16:00",status:"Pending",reason:"Control de crecimiento",modality:"Presencial"},
      {id:"seed3",doctorId:"doc_03",doctorName:"Dra. Andrea López",specialtyName:"Dermatología",patientName:"Laura Jiménez",patientAge:34,date:ds[0],time:"09:00",status:"Cancelled",reason:"Revisión de lunar",modality:"Telemedicina"}
    ];
    this.saveAppointments(seed); return seed;
  },
  saveAppointments(list){ localStorage.setItem(this.apptKey, JSON.stringify(list)); }
};

/* ---------- 3. UTILIDADES ---------- */
function fmtISO(d){ return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`; }
function selectableDates(){ const out=[]; const c=new Date(); c.setHours(0,0,0,0);
  while(out.length<7){ if(c.getDay()!==0) out.push(new Date(c)); c.setDate(c.getDate()+1); } return out; }
function dateObj(iso){ const [y,m,d]=iso.split('-').map(Number); return new Date(y,m-1,d); }
function prettyDate(iso){ const d=dateObj(iso); return `${WEEKDAYS[d.getDay()]} ${d.getDate()} ${MONTHS[d.getMonth()]}`; }
function prettyLong(iso){ const d=dateObj(iso); const wd=["domingo","lunes","martes","miércoles","jueves","viernes","sábado"][d.getDay()];
  const mm=["enero","febrero","marzo","abril","mayo","junio","julio","agosto","septiembre","octubre","noviembre","diciembre"][d.getMonth()];
  return `${wd} ${d.getDate()} de ${mm} ${d.getFullYear()}`; }
function initials(name){ const c=name.replace(/Dr\. |Dra\. /,''); const p=c.split(/\s+/); let r=(p[0]?.[0]||'?').toUpperCase(); if(p.length>1&&p[p.length-1]) r+=(p[p.length-1][0]||'').toUpperCase(); return r; }
function displayName(full){ const female=(full.split(' ')[0]||'').toLowerCase().endsWith('a'); return (female?'Dra. ':'Dr. ')+full; }
function timeRange(t){ const h=parseInt(t.slice(0,2)); return `${t} – ${String(Math.min(h+1,23)).padStart(2,'0')}:00`; }
function period(t){ return parseInt(t.slice(0,2))<14?'morning':'afternoon'; }
function getDoctor(id){ return DOCTORS.find(d=>d.id===id); }

function availableSlots(doctorId){
  const slots=[]; const cal=new Date(); cal.setHours(0,0,0,0); let di=0, created=0;
  while(created<7){
    if(cal.getDay()===0){ cal.setDate(cal.getDate()+1); di++; continue; }
    const date=fmtISO(cal);
    HOURS.forEach((h,i)=>slots.push({date,time:h,available:((di*7+i)%4)!==0}));
    cal.setDate(cal.getDate()+1); di++; created++;
  }
  return slots;
}

/* ---------- 4. ESTADO GLOBAL ---------- */
const state = {
  screen:"login",
  isRegister:false,
  loginError:"",
  homeSpecialty:null,
  homeSearch:"",
  currentDoctor:null,
  currentAppointment:null,
  apptFilter:"all",
  // wizard
  wStep:1, wDoctor:null, wSpecialty:null, wDate:null, wSlot:null, wReason:"", wModality:"Presencial", wReminder:true,
  busy:false
};

/* ---------- 5. ICONOS (SVG inline) ---------- */
const ICON = {
  logo:'<svg width="20" height="20" viewBox="0 0 24 24" fill="none"><rect x="10.2" y="3" width="3.6" height="18" rx="1.8" fill="#2563eb"/><rect x="3" y="10.2" width="18" height="3.6" rx="1.8" fill="#10b981"/><circle cx="12" cy="12" r="1.3" fill="#fff"/></svg>',
  search:'<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="11" cy="11" r="7"/><path d="m20 20-3.5-3.5"/></svg>',
  filter:'<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M4 6h16M7 12h10M10 18h4"/></svg>',
  clock:'<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/></svg>',
  calendar:'<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><rect x="3" y="4" width="18" height="18" rx="3"/><path d="M3 9h18M8 2v4M16 2v4"/></svg>',
  chevron:'<svg class="chev" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round"><path d="m9 6 6 6-6 6"/></svg>',
  back:'<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round"><path d="m15 6-6 6 6 6"/></svg>',
  star:'<svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2l3 6.5 7 .6-5.3 4.6 1.6 6.9L12 17l-6.3 3.6 1.6-6.9L2 9.1l7-.6z"/></svg>',
  check:'<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><path d="m5 13 4 4L19 7"/></svg>',
  checkSeal:'<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2l2.4 1.8 3-.2 1 2.8 2.6 1.6-1 2.8 1 2.8-2.6 1.6-1 2.8-3-.2L12 22l-2.4-1.8-3 .2-1-2.8L3 15.8l1-2.8-1-2.8 2.6-1.6 1-2.8 3 .2z"/><path d="m9 12 2 2 4-4" stroke-width="2.4" stroke-linecap="round"/></svg>',
  steth:'<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M6 3v6a5 5 0 0 0 10 0V3"/><path d="M4 3h3M15 3h3M11 14v3a4 4 0 0 0 8 0v-2"/><circle cx="19" cy="13" r="2"/></svg>',
  person:'<svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="12" cy="8" r="4"/><path d="M4 21a8 8 0 0 1 16 0"/></svg>',
  calendarTab:'<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><rect x="3" y="4" width="18" height="18" rx="3"/><path d="M3 9h18M8 2v4M16 2v4M12 13v4M10 15h4"/></svg>',
  home:'<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M3 10.5 12 3l9 7.5"/><path d="M5 9.5V21h14V9.5"/></svg>',
  mail:'<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><rect x="3" y="5" width="18" height="14" rx="3"/><path d="m4 7 8 6 8-6"/></svg>',
  lock:'<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><rect x="4" y="10" width="16" height="11" rx="3"/><path d="M8 10V7a4 4 0 0 1 8 0v3"/></svg>',
  building:'<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><rect x="4" y="3" width="16" height="18" rx="2"/><path d="M9 7h.01M15 7h.01M9 11h.01M15 11h.01M9 15h.01M15 15h.01"/></svg>',
  video:'<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><rect x="3" y="6" width="13" height="12" rx="3"/><path d="m16 10 5-3v10l-5-3z"/></svg>',
  euro:'<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M16 7a6 6 0 1 0 0 10M5 10h8M5 14h8"/></svg>',
  sun:'<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M2 12h2M20 12h2M5 5l1.5 1.5M17.5 17.5 19 19M19 5l-1.5 1.5M6.5 17.5 5 19"/></svg>',
  sunset:'<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M3 18h18M6 14a6 6 0 0 1 12 0M12 2v3M5 6l1.5 1.5M19 6l-1.5 1.5"/></svg>',
  logout:'<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M15 4h3a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2h-3"/><path d="M10 17l-5-5 5-5M5 12h11"/></svg>',
  plus:'<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.6" stroke-linecap="round"><path d="M12 5v14M5 12h14"/></svg>',
  close:'<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round"><path d="M6 6l12 12M18 6 6 18"/></svg>',
  bell:'<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M6 9a6 6 0 0 1 12 0c0 5 2 6 2 6H4s2-1 2-6"/><path d="M9 20a3 3 0 0 0 6 0"/></svg>',
  empty:'<svg width="38" height="38" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><rect x="3" y="4" width="18" height="18" rx="3"/><path d="M3 9h18M8 2v4M16 2v4"/><path d="M12 13v4M10 15h4"/></svg>'
};

/* ---------- 6. HELPERS UI ---------- */
function statusSpan(s){ const map={Confirmed:['confirmed','Confirmada'],Pending:['pending','Pendiente'],Cancelled:['cancelled','Cancelada']};
  const [cls,txt]=map[s]||['confirmed','Confirmada']; return `<span class="badge ${cls}"><span class="b-dot"></span>${txt}</span>`; }
function avatar(name,size='md'){ return `<div class="avatar ${size}">${initials(name)}</div>`; }
function toast(msg){ const t=document.getElementById('toast'); if(!t)return; t.textContent=msg; t.classList.add('on'); clearTimeout(t._t); t._t=setTimeout(()=>t.classList.remove('on'),2600); }

/* ---------- 7. RENDER ---------- */
const app = document.getElementById('app');

function render(){
  let html='';
  const base = `<div class="toast" id="toast"></div>`;
  switch(state.screen){
    case 'login': html=screenLogin(); break;
    case 'doctorDetail': html=screenDoctorDetail(); break;
    case 'appointmentDetail': html=screenAppointmentDetail(); break;
    case 'wizard': html=screenWizard(); break;
    default: html=screenTabbar(); break;
  }
  app.innerHTML = base + html;
  afterRender();
}

function afterRender(){
  // Focus ring en inputs
  app.querySelectorAll('.input-wrap input').forEach(inp=>{
    inp.addEventListener('focus',()=>inp.parentElement.classList.add('focus'));
    inp.addEventListener('blur',()=>{ setTimeout(()=>inp.parentElement.classList.remove('focus'),120); });
  });
  const sc = app.querySelector('.screen'); if(sc) sc.scrollTop = state._scroll||0;
}

/* ===== 7.1 LOGIN ===== */
function screenLogin(){
  return `
  <div class="screen"><div class="pad stack" style="padding-top:40px">
    <div class="stack center-col" style="gap:14px">
      <div class="avatar lg" style="background:var(--primary-soft)">${ICON.logo}</div>
      <div class="center">
        <h1 style="font-size:28px">MediCare</h1>
        <p class="sub" style="margin-top:6px">${state.isRegister?'Crea tu cuenta de paciente':'Tu salud, a un toque de distancia'}</p>
      </div>
      <div class="row" style="justify-content:center">
        <span class="pill-clinic"><span class="dot"></span>Clínica Central</span>
        <span class="pill-clinic" style="background:var(--primary-soft);color:var(--primary-on-soft)">
          <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/></svg>Citas online</span>
      </div>
    </div>

    <div class="card stack">
      <h2>${state.isRegister?'Crear cuenta':'Iniciar sesión'}</h2>
      ${state.isRegister?`
      <div class="field"><label>Nombre completo</label>
        <div class="input-wrap">${ICON.person.replace('26','16')}<input id="fName" placeholder="Ej. Laura Jiménez" value="${state._name||''}"/></div></div>`:''}
      <div class="field"><label>Correo electrónico</label>
        <div class="input-wrap">${ICON.mail}<input id="fEmail" type="email" placeholder="tucorreo@ejemplo.com" value="${state._email||''}"/></div></div>
      <div class="field"><label>Contraseña</label>
        <div class="input-wrap">${ICON.lock}<input id="fPass" type="password" placeholder="Mínimo 4 caracteres"/></div></div>
      ${state.isRegister?`
      <div class="field"><label>Edad</label>
        <div class="input-wrap">${ICON.calendar}<input id="fAge" type="number" placeholder="Ej. 34" value="${state._age||''}"/></div></div>`:''}
      ${state.loginError?`<div class="err"><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4"><circle cx="12" cy="12" r="9"/><path d="M12 8v5M12 16h.01"/></svg>${state.loginError}</div>`:''}
      <button class="btn btn-primary btn-block" id="btnAuth" ${state.busy?'disabled':''}>
        ${state.busy?'<span class="spinner" style="width:18px;height:18px;border-width:2px;border-top-color:#fff;border-color:rgba(255,255,255,.4);border-top-color:#fff"></span>':''}
        ${state.isRegister?'Registrarme':'Entrar'}
      </button>
    </div>

    <div class="row" style="justify-content:center;gap:4px">
      <span class="sub">${state.isRegister?'¿Ya tienes cuenta?':'¿No tienes cuenta?'}</span>
      <button class="btn btn-ghost" id="btnToggle" style="color:var(--primary);font-weight:600">${state.isRegister?'Inicia sesión':'Regístrate'}</button>
    </div>
    <p class="small center">Demo de la app iOS · datos locales de ejemplo</p>
  </div></div>`;
}

/* ===== 7.2 TABS (home / citas / perfil) ===== */
function screenTabbar(){
  const active = state.screen==='appointments'?'appointments':state.screen==='profile'?'profile':'home';
  let content = '';
  if(active==='home') content=screenHome();
  else if(active==='appointments') content=screenAppointments();
  else content=screenProfile();
  return content + tabbar(active);
}
function tabbar(active){
  const t=(id,label,icon)=>`<button class="tab ${active===id?'active':''}" data-nav="${id}">
    <span class="t-ic">${icon}</span>${label}</button>`;
  return `<div class="tabbar">
    ${t('home','Inicio',ICON.home)}${t('appointments','Mis Citas',ICON.calendarTab)}${t('profile','Perfil',ICON.person.replace('26','20'))}
  </div>`;
}

/* ===== 7.3 HOME ===== */
function screenHome(){
  const user = DB.getUser()||{fullName:'Paciente'};
  const appts = DB.loadAppointments();
  const confirmed = appts.filter(a=>a.status==='Confirmed').length;
  const pending = appts.filter(a=>a.status==='Pending').length;

  let docs = DOCTORS.slice().sort((a,b)=>b.rating-a.rating);
  if(state.homeSpecialty) docs = docs.filter(d=>d.sp===state.homeSpecialty);
  if(state.homeSearch){ const q=state.homeSearch.toLowerCase();
    docs = docs.filter(d=>d.fullName.toLowerCase().includes(q)||d.spName.toLowerCase().includes(q)||d.hospital.toLowerCase().includes(q)); }

  const specChips = `<button class="chip ${!state.homeSpecialty?'active':''}" data-spec="">🩺 Todas</button>` +
    SPECIALTIES.map(s=>`<button class="chip ${state.homeSpecialty===s.id?'active':''}" data-spec="${s.id}">${s.emoji} ${s.name}</button>`).join('');

  const docList = docs.length ? docs.map(d=>`
    <div class="card doc-row item" data-doc="${d.id}">
      ${avatar(d.fullName,'md')}
      <div class="meta">
        <div class="name">${displayName(d.fullName)}</div>
        <div class="small" style="color:var(--primary);font-weight:600">${d.spName}</div>
        <div class="row gap-s" style="margin-top:3px">
          <span class="stars">${ICON.star}${d.rating.toFixed(1)}</span>
          <span class="small">· ${d.years} años exp.</span>
        </div>
      </div>
      ${ICON.chevron}
    </div>`).join('')
    : `<div class="card center" style="padding:32px"><div class="empty-ic">${ICON.empty}</div>
        <h3>Sin resultados</h3><p class="sub" style="margin-top:4px">Prueba otra especialidad o término.</p></div>`;

  const now = new Date(); const wd=["domingo","lunes","martes","miércoles","jueves","viernes","sábado"][now.getDay()];
  const mm=["enero","febrero","marzo","abril","mayo","junio","julio","agosto","septiembre","octubre","noviembre","diciembre"][now.getMonth()];
  const firstName = (user.fullName||'Paciente').split(' ')[0];

  return `<div class="screen"><div class="pad stack">
    <div class="row" style="align-items:flex-start">
      <div style="flex:1;min-width:0">
        <div class="row gap-s"><span style="color:var(--primary)">${ICON.calendar}</span><span class="small" style="font-weight:600">${wd.charAt(0).toUpperCase()+wd.slice(1)}, ${now.getDate()} ${mm}</span></div>
        <h1 style="font-size:22px;margin-top:4px">Hola, ${firstName}</h1>
        <p class="sub">¿Agendamos tu próxima consulta?</p>
      </div>
      <button class="btn btn-primary btn-sm" data-nav="wizard" style="width:auto;flex-shrink:0">${ICON.plus} Nueva Cita</button>
    </div>

    <div class="metrics">
      <div class="metric"><div class="m-ic" style="background:var(--mint-fill);color:var(--mint-dark)">${ICON.checkSeal}</div>
        <div class="m-val num">${confirmed}</div><div class="m-lbl">Confirmadas</div></div>
      <div class="metric"><div class="m-ic" style="background:var(--amber-fill);color:var(--amber-dark)">${ICON.clock}</div>
        <div class="m-val num">${pending}</div><div class="m-lbl">Pendientes</div></div>
      <div class="metric"><div class="m-ic" style="background:var(--primary-soft);color:var(--primary-on-soft)">${ICON.calendar}</div>
        <div class="m-val num">${appts.length}</div><div class="m-lbl">Total</div></div>
    </div>

    <div class="searchbar">
      <div class="input-wrap">${ICON.search}<input id="homeSearch" placeholder="Buscar médico o especialidad..." value="${state.homeSearch}"/></div>
      <button class="icon-btn" title="Filtros">${ICON.filter}</button>
    </div>

    <div>
      <div class="section-title"><h2>Especialidades</h2></div>
      <div class="chips" id="specChips">${specChips}</div>
    </div>

    <div>
      <div class="section-title"><h2>Médicos disponibles</h2><span class="small">${docs.length} resultados</span></div>
      <div class="list">${docList}</div>
    </div>
  </div></div>`;
}

/* ===== 7.4 MIS CITAS ===== */
function screenAppointments(){
  const all = DB.loadAppointments();
  const c = all.filter(a=>a.status==='Confirmed').length;
  const p = all.filter(a=>a.status==='Pending').length;
  const x = all.filter(a=>a.status==='Cancelled').length;

  const filters = {all:'Todas',Confirmed:'Confirmadas',Pending:'Pendientes',Cancelled:'Canceladas'};
  const filterChips = Object.entries(filters).map(([k,v])=>
    `<button class="chip ${state.apptFilter===k?'active':''}" data-filter="${k}">${v}</button>`).join('');

  const list = state.apptFilter==='all'? all : all.filter(a=>a.status===state.apptFilter);
  const listHtml = list.length ? list.map(a=>`
    <div class="card appt-card ${a.status==='Confirmed'?'confirmed':a.status==='Pending'?'pending':'cancelled'} item" data-appt="${a.id}">
      <div class="appt-top">
        <span class="appt-time" style="color:var(--primary)">${ICON.clock}${timeRange(a.time)}</span>
        ${statusSpan(a.status)}
      </div>
      <div class="divider" style="margin:0 0 12px"></div>
      <div class="row">
        ${avatar(a.doctorName,'sm')}
        <div style="flex:1;min-width:0">
          <div class="name" style="font-family:var(--font-head);font-weight:600;font-size:15px;color:var(--text)">${a.doctorName}</div>
          <div class="row gap-s small">${ICON.steth}<span>${a.specialtyName}</span></div>
        </div>
        ${ICON.chevron}
      </div>
      ${a.reason?`<p class="small" style="margin-top:8px">${a.reason}</p>`:''}
      <div class="row gap-s small" style="margin-top:6px">${ICON.calendar}<span>${prettyDate(a.date)}</span></div>
    </div>`).join('')
    : `<div class="card center" style="padding:32px"><div class="empty-ic">${ICON.empty}</div>
        <h3>Sin citas</h3><p class="sub" style="margin-top:4px">Agenda una nueva cita médica para verla aquí.</p></div>`;

  return `<div class="screen">
    <div class="appbar"><div class="brand"><div><h1 style="font-size:20px">Mis Citas</h1><span class="small">${all.length} citas en total</span></div></div></div>
    <div class="pad stack">
      <div class="metrics">
        <div class="metric" style="background:var(--mint-fill);border-color:transparent"><div class="m-val num" style="color:var(--mint-dark)">${c}</div><div class="m-lbl">Confirmadas</div></div>
        <div class="metric" style="background:var(--amber-fill);border-color:transparent"><div class="m-val num" style="color:var(--amber-dark)">${p}</div><div class="m-lbl">Pendientes</div></div>
        <div class="metric" style="background:var(--danger-fill);border-color:transparent"><div class="m-val num" style="color:var(--danger-dark)">${x}</div><div class="m-lbl">Canceladas</div></div>
      </div>
      <div class="chips" id="filterChips">${filterChips}</div>
      <div class="list">${listHtml}</div>
      <button class="btn btn-soft btn-block" data-nav="wizard">${ICON.plus} Agendar nueva cita</button>
    </div>
  </div>`;
}

/* ===== 7.5 PERFIL ===== */
function screenProfile(){
  const u = DB.getUser()||{fullName:'Paciente',email:'',phone:'',age:0};
  const appts = DB.loadAppointments();
  const confirmed = appts.filter(a=>a.status==='Confirmed').length;
  const row=(icon,label,value)=>`<div class="row" style="gap:12px;padding:12px 0">
    <span style="color:var(--primary);width:24px;display:grid;place-items:center">${icon}</span>
    <div style="flex:1"><div class="small">${label}</div><div class="body" style="color:var(--text)">${value}</div></div></div>`;
  return `<div class="screen">
    <div class="appbar"><div class="brand"><div><h1 style="font-size:20px">Mi Perfil</h1></div></div></div>
    <div class="pad stack">
      <div class="card stack center-col" style="gap:10px;padding-top:24px">
        ${avatar(u.fullName,'lg')}
        <div class="center"><h2>${u.fullName}</h2><p class="sub">${u.email}</p></div>
        <span class="badge confirmed"><span class="b-dot"></span>Paciente verificado</span>
      </div>
      <div class="metrics">
        <div class="metric"><div class="m-val num">${appts.length}</div><div class="m-lbl">Citas totales</div></div>
        <div class="metric"><div class="m-val num">${u.age||'—'}</div><div class="m-lbl">Edad</div></div>
        <div class="metric"><div class="m-val num" style="color:var(--mint)">${confirmed}</div><div class="m-lbl">Activas</div></div>
      </div>
      <div class="card">
        <h3 style="margin-bottom:4px">Información personal</h3>
        ${row(ICON.person.replace('26','16'),'Nombre completo',u.fullName)}
        <div class="divider" style="margin:0"></div>
        ${row(ICON.mail,'Correo',u.email||'—')}
        <div class="divider" style="margin:0"></div>
        ${row(ICON.calendar,'Edad',(u.age?u.age+' años':'No especificada'))}
      </div>
      <div class="card" style="padding-top:4px;padding-bottom:4px">
        <div class="row" style="gap:12px;padding:12px 0;color:var(--muted)">${ICON.bell}<div style="flex:1"><div class="label" style="color:var(--text)">Notificaciones</div><div class="small">Recordatorios de citas</div></div></div>
        <div class="divider" style="margin:0"></div>
        <button class="btn btn-ghost btn-block" id="btnLogout" style="justify-content:flex-start;color:var(--danger-dark);padding:14px 0;width:100%">
          <span style="color:var(--danger)">${ICON.logout}</span> Cerrar sesión</button>
      </div>
    </div>
  </div>`;
}

/* ===== 7.6 DETALLE DOCTOR ===== */
function screenDoctorDetail(){
  const d = state.currentDoctor; if(!d) return '';
  const slots = availableSlots(d.id);
  const dates = selectableDates();
  const curDate = state.ddDate || fmtISO(dates[0]);
  const daySlots = slots.filter(s=>s.date===curDate);

  const dateChips = dates.map(dt=>{
    const iso=fmtISO(dt); const sel=iso===curDate;
    return `<div class="date ${sel?'sel':''}" data-ddDate="${iso}"><span class="d-w">${WEEKDAYS[dt.getDay()]}</span><span class="d-n num">${dt.getDate()}</span></div>`;
  }).join('');

  const renderPeriod=(label,icon,per)=>{
    const list=daySlots.filter(s=>period(s.time)===per); if(!list.length) return '';
    return `<div class="period">${icon}${label}</div><div class="slots">`+
      list.map(s=>`<div class="slot ${!s.available?'off':(state.ddSlot===s.time?'sel':'')}" ${s.available?`data-ddSlot="${s.time}"`:''}>${s.time}</div>`).join('')+
      `</div>`;
  };

  const fee = d.fee.toLocaleString('es-ES',{minimumFractionDigits:0})+' €';
  return `<div class="screen">
    <div class="modal-head"><button class="icon-back" data-back="home">${ICON.back}</button>
      <div><div style="font-family:var(--font-head);font-weight:600;font-size:16px;color:var(--text)">Agendar cita</div><div class="small">${d.spName}</div></div></div>
    <div class="pad stack" style="padding-bottom:110px">
      <div class="card stack center-col" style="gap:10px;padding-top:22px">
        ${avatar(d.fullName,'lg')}
        <div class="center"><h2>${displayName(d.fullName)}</h2><p style="color:var(--primary);font-weight:600">${d.spName}</p></div>
        <div class="row" style="justify-content:center;flex-wrap:wrap;gap:8px">
          <span class="pill-info" style="background:var(--amber-fill);color:var(--amber-dark)">${ICON.star}${d.rating.toFixed(1)}</span>
          <span class="pill-info" style="background:var(--primary-soft);color:var(--primary-on-soft)">${d.years} años exp.</span>
          <span class="pill-info" style="background:var(--mint-fill);color:var(--mint-dark)">${fee}</span>
        </div>
        <div class="who" style="color:var(--body)">${ICON.building}${d.hospital}</div>
      </div>
      <div class="card"><h3 style="margin-bottom:8px">Sobre el especialista</h3><p class="body">${d.bio}</p></div>
      <div><div class="section-title"><h2>Fecha y Horario</h2></div><div class="dates">${dateChips}</div></div>
      ${renderPeriod('Turno Mañana',ICON.sun,'morning')}
      ${renderPeriod('Turno Tarde',ICON.sunset,'afternoon')}
      <div class="field"><label>Motivo de consulta <span class="small">(opcional)</span></label>
        <div class="input-wrap" style="height:auto;padding:12px 14px">
          <textarea id="ddReason" placeholder="Ej. Control rutinario..." style="border:none;outline:none;width:100%;font-family:var(--font-body);font-size:14px;resize:none;height:70px;color:var(--text)">${state.ddReason||''}</textarea>
        </div></div>
    </div>
    <div class="actionbar">
      <div style="flex:1">
        ${state.ddSlot?`<div class="row gap-s small" style="color:var(--mint-dark);font-weight:600;margin-bottom:6px">${ICON.check}${prettyDate(curDate)} · ${state.ddSlot}</div>`:'<div class="small" style="margin-bottom:6px">Selecciona una hora</div>'}
        <button class="btn btn-primary" id="ddConfirm" ${state.ddSlot?'':'disabled'}>${ICON.checkSeal} Confirmar y Reservar</button>
      </div>
    </div>
  </div>`;
}

/* ===== 7.7 WIZARD NUEVA CITA ===== */
function screenWizard(){
  const steps = ['Médico','Horario','Detalles'];
  const stepBar = `<div class="card pad-s steps">
    ${steps.map((s,i)=>{ const idx=i+1; const done=state.wStep>idx; const active=state.wStep===idx;
      return `${i>0?`<div class="s-line ${state.wStep>i?'done':''}"></div>`:''}
        <div class="step ${done?'done':''} ${active?'active':''}"><div class="s-ic">${done?ICON.check:idx}</div><span class="s-lbl">${s}</span></div>`;
    }).join('')}
  </div>`;

  let body='';
  if(state.wStep===1){
    const list = DOCTORS.filter(d=>!state.wSpecialty||d.sp===state.wSpecialty);
    const specChips = SPECIALTIES.map(s=>`<button class="chip ${state.wSpecialty===s.id?'active':''}" data-wSpec="${s.id}">${s.emoji} ${s.name}</button>`).join('');
    body = `<div class="card row gap-s">${avatar((DB.getUser()||{}).fullName||'Paciente','sm')}
        <div style="flex:1"><div class="small">PACIENTE VERIFICADO</div><div class="label" style="color:var(--text);font-size:15px">${(DB.getUser()||{}).fullName||'Paciente'}</div></div>
        <span style="color:var(--mint)">${ICON.checkSeal}</span></div>
      <div><div class="section-title"><h2>Especialidad</h2></div><div class="chips">${specChips}</div></div>
      <div><div class="section-title"><h2>Especialista</h2></div>
      <div class="list">${list.map(d=>`
        <div class="card doc-row item" data-wDoc="${d.id}" style="${state.wDoctor===d.id?'border-color:var(--primary);border-width:1.5px':''}">
          ${avatar(d.fullName,'sm')}<div class="meta"><div class="name">${displayName(d.fullName)}</div><div class="small">${d.spName} · ${d.hospital}</div></div>
          <span style="color:${state.wDoctor===d.id?'var(--primary)':'var(--muted)'}">${state.wDoctor===d.id?ICON.checkSeal.replace('<svg','<svg style="opacity:1"') :'<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="9"/></svg>'}</span>
        </div>`).join('')}</div></div>`;
  } else if(state.wStep===2){
    const d = getDoctor(state.wDoctor);
    const dates = selectableDates();
    const curDate = state.wDate||fmtISO(dates[0]);
    const daySlots = availableSlots(state.wDoctor).filter(s=>s.date===curDate);
    const dateChips = dates.map(dt=>{ const iso=fmtISO(dt); const sel=iso===curDate;
      return `<div class="date ${sel?'sel':''}" data-wDate="${iso}"><span class="d-w">${WEEKDAYS[dt.getDay()]}</span><span class="d-n num">${dt.getDate()}</span></div>`; }).join('');
    const per=(label,icon,p)=>{ const l=daySlots.filter(s=>period(s.time)===p); if(!l.length) return '';
      return `<div class="period">${icon}${label}</div><div class="slots">`+l.map(s=>`<div class="slot ${!s.available?'off':(state.wSlot===s.time?'sel':'')}" ${s.available?`data-wSlot="${s.time}"`:''}>${s.time}</div>`).join('')+`</div>`; };
    body = `<div class="card row gap-s">${avatar(d.fullName,'sm')}<div style="flex:1"><div class="small">ESPECIALISTA ASIGNADO</div><div class="label" style="color:var(--text);font-size:15px">${displayName(d.fullName)}</div></div>
        <button class="btn btn-ghost" data-wStep="1" style="color:var(--primary)">Cambiar</button></div>
      <div><div class="section-title"><h2>Fecha</h2></div><div class="dates">${dateChips}</div></div>
      ${per('Turno Mañana',ICON.sun,'morning')}${per('Turno Tarde',ICON.sunset,'afternoon')}`;
  } else {
    const d = getDoctor(state.wDoctor);
    body = `<div><div class="section-title"><h2>Modalidad</h2></div>
      <div class="row gap-s">
        <button class="btn ${state.wModality==='Presencial'?'btn-primary':'btn-soft'}" data-wMod="Presencial" style="flex:1;flex-direction:column;height:78px">${ICON.building} Presencial</button>
        <button class="btn ${state.wModality==='Telemedicina'?'btn-primary':'btn-soft'}" data-wMod="Telemedicina" style="flex:1;flex-direction:column;height:78px">${ICON.video} Telemedicina</button>
      </div></div>
      <div class="field"><label>Motivo de consulta <span class="small">(opcional)</span></label>
        <div class="input-wrap" style="height:auto;padding:12px 14px"><textarea id="wReason" placeholder="Ej. Control rutinario..." style="border:none;outline:none;width:100%;font-family:var(--font-body);font-size:14px;resize:none;height:80px;color:var(--text)">${state.wReason||''}</textarea></div></div>
      <div class="card row" style="justify-content:space-between">
        <div><div class="label" style="color:var(--text)">Recordatorio digital</div><div class="small">Notificación 24 h antes</div></div>
        <button id="wReminder" style="width:50px;height:30px;border-radius:999px;border:none;cursor:pointer;background:${state.wReminder?'var(--mint)':'var(--surface-2)'};position:relative">
          <span style="position:absolute;top:3px;${state.wReminder?'right:3px':'left:3px'};width:24px;height:24px;border-radius:50%;background:#fff;transition:.2s"></span></button>
      </div>
      <div class="card"><h3 style="margin-bottom:8px">Resumen</h3>
        <div class="who" style="margin-bottom:8px">${ICON.steth}${displayName(d.fullName)}</div>
        <div class="who" style="margin-bottom:8px">${ICON.calendar}${prettyDate(state.wDate)} · ${state.wSlot}</div>
        <div class="who">${state.wModality==='Presencial'?ICON.building:ICON.video}${state.wModality}</div></div>`;
  }

  const canNext = state.wStep===1? !!state.wDoctor : state.wStep===2? !!state.wSlot : true;
  const nextLabel = state.wStep<3?'Continuar':'Confirmar y Reservar';
  return `<div class="screen">
    <div class="modal-head"><button class="icon-back" id="wClose">${ICON.close}</button>
      <div style="font-family:var(--font-head);font-weight:600;font-size:16px;color:var(--text)">Nueva Cita</div></div>
    <div class="pad stack" style="padding-bottom:100px">${stepBar}${body}</div>
    <div class="actionbar">
      ${state.wStep>1?`<button class="btn btn-soft" id="wPrev" style="max-width:120px">Atrás</button>`:''}
      <button class="btn btn-primary" id="wNext" ${canNext?'':'disabled'} style="flex:1">${ICON.checkSeal} ${nextLabel}</button>
    </div>
  </div>`;
}

/* ===== 7.8 DETALLE CITA ===== */
function screenAppointmentDetail(){
  const a = state.currentAppointment; if(!a) return '';
  const d = getDoctor(a.doctorId);
  const accent = a.status==='Confirmed'?'var(--mint)':a.status==='Pending'?'var(--amber)':'var(--danger)';
  return `<div class="screen">
    <div class="modal-head"><button class="icon-back" data-back="appointments">${ICON.back}</button>
      <div style="font-family:var(--font-head);font-weight:600;font-size:16px;color:var(--text)">Detalle de Cita</div></div>
    <div class="pad stack" style="padding-bottom:110px">
      <div class="card" style="position:relative;overflow:hidden">
        <span style="position:absolute;left:0;top:14px;bottom:14px;width:4px;background:${accent};border-radius:3px"></span>
        <div class="row" style="justify-content:space-between;margin-bottom:10px">${statusSpan(a.status)}<span class="small">#${a.id.slice(0,8).toUpperCase()}</span></div>
        <h2 style="font-size:18px;text-transform:capitalize">${prettyLong(a.date)}</h2>
        <div class="appt-time" style="color:var(--primary);margin-top:6px">${ICON.clock}${timeRange(a.time)}</div>
      </div>
      <div class="card">
        <div class="small" style="margin-bottom:10px">ESPECIALISTA ASIGNADO</div>
        <div class="row gap-s">${avatar(a.doctorName,'md')}
          <div style="flex:1"><div class="name" style="font-family:var(--font-head);font-weight:600;color:var(--text)">${a.doctorName}</div>
            <div style="color:var(--primary);font-weight:600;font-size:13px">${a.specialtyName}</div>
            ${d?`<div class="small">${d.hospital} · ${d.years} años exp.</div>`:''}</div></div>
      </div>
      ${a.reason?`<div class="card"><h3 style="margin-bottom:8px">Motivo de consulta</h3><p class="body">${a.reason}</p></div>`:''}
      <div class="card">
        <h3 style="margin-bottom:4px">Datos del paciente</h3>
        <div class="row" style="justify-content:space-between;padding:10px 0"><span class="body">Paciente</span><span class="label" style="color:var(--text)">${a.patientName}</span></div>
        <div class="divider" style="margin:0"></div>
        <div class="row" style="justify-content:space-between;padding:10px 0"><span class="body">Edad</span><span class="label" style="color:var(--text)">${a.patientAge} años</span></div>
        ${a.modality?`<div class="divider" style="margin:0"></div><div class="row" style="justify-content:space-between;padding:10px 0"><span class="body">Modalidad</span><span class="label" style="color:var(--text)">${a.modality}</span></div>`:''}
      </div>
    </div>
    <div class="actionbar">
      ${a.status!=='Cancelled'
        ? `<button class="btn btn-primary" style="flex:1" data-start>${ICON.video} Iniciar Consulta</button>
           <button class="btn btn-danger" style="flex:1" id="adCancel">Cancelar cita</button>`
        : `<div class="card" style="flex:1;text-align:center;color:var(--muted)">Esta cita está cancelada</div>`}
    </div>
  </div>`;
}

/* ---------- 8. EVENTOS ---------- */
app.addEventListener('click', (e)=>{
  const el = e.target.closest('[data-nav],[data-spec],[data-doc],[data-appt],[data-filter],[data-ddDate],[data-ddSlot],[data-wSpec],[data-wDoc],[data-wStep],[data-wDate],[data-wSlot],[data-wMod],[data-back]');
  const t = e.target;

  // Botones específicos
  if(t.closest('#btnAuth')){ handleAuth(); return; }
  if(t.closest('#btnToggle')){ state.isRegister=!state.isRegister; state.loginError=''; render(); return; }
  if(t.closest('#btnLogout')){ if(confirm('¿Cerrar sesión?')){ DB.clearUser(); state.screen='login'; render(); } return; }
  if(t.closest('#ddConfirm')){ ddConfirm(); return; }
  if(t.closest('#wClose')){ state.screen='home'; render(); return; }
  if(t.closest('#wPrev')){ state.wStep--; render(); return; }
  if(t.closest('#wNext')){ wNext(); return; }
  if(t.closest('#wReminder')){ state.wReminder=!state.wReminder; render(); return; }
  if(t.closest('#adCancel')){ cancelAppointment(state.currentAppointment.id); return; }
  if(t.closest('[data-start]')){ toast('🩺 Iniciando sala virtual... (demo)'); return; }

  if(!el) return;
  const d = el.dataset;

  if(d.nav){ if(d.nav==='wizard'){ startWizard(); }
    else { state.screen=d.nav; state._scroll=0; render(); } return; }

  if(d.spec!==undefined){ state.homeSpecialty = d.spec||null; render(); return; }
  if(d.filter){ state.apptFilter=d.filter; render(); return; }
  if(d.doc){ state.currentDoctor=getDoctor(d.doc); state.ddDate=null; state.ddSlot=null; state.ddReason=''; state.screen='doctorDetail'; render(); return; }
  if(d.appt){ state.currentAppointment=DB.loadAppointments().find(a=>a.id===d.appt); state.screen='appointmentDetail'; render(); return; }
  if(d.ddDate){ state.ddDate=d.ddDate; state.ddSlot=null; render(); return; }
  if(d.ddSlot){ state.ddSlot=d.ddSlot; render(); return; }
  if(d.wSpec){ state.wSpecialty=d.wSpecialty; state.wDoctor=null; render(); return; }
  if(d.wDoc){ state.wDoctor=d.wDoc; state.wSlot=null; state.wDate=null; render(); return; }
  if(d.wStep){ state.wStep=parseInt(d.wStep); render(); return; }
  if(d.wDate){ state.wDate=d.wDate; state.wSlot=null; render(); return; }
  if(d.wSlot){ state.wSlot=d.wSlot; render(); return; }
  if(d.wMod){ state.wModality=d.wMod; render(); return; }
  if(d.back){ state.screen=d.back; render(); return; }
});

// Búsqueda en vivo
app.addEventListener('input',(e)=>{
  if(e.target.id==='homeSearch'){ state.homeSearch=e.target.value; const val=e.target.value; render();
    const ni=document.getElementById('homeSearch'); if(ni){ ni.focus(); ni.setSelectionRange(val.length,val.length); } }
  if(e.target.id==='ddReason'){ state.ddReason=e.target.value; }
  if(e.target.id==='wReason'){ state.wReason=e.target.value; }
  if(e.target.id==='fName'){ state._name=e.target.value; }
  if(e.target.id==='fEmail'){ state._email=e.target.value; }
  if(e.target.id==='fAge'){ state._age=e.target.value; }
});

/* ---------- 9. ACCIONES ---------- */
function handleAuth(){
  const email=(document.getElementById('fEmail')?.value||'').trim();
  const pass=document.getElementById('fPass')?.value||'';
  const name=(document.getElementById('fName')?.value||'').trim();
  const age=parseInt(document.getElementById('fAge')?.value||'0')||0;
  const emailRe=/^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;

  if(!emailRe.test(email)){ state.loginError='Ingresa un correo electrónico válido'; render(); return; }
  if(pass.length<4){ state.loginError='La contraseña debe tener al menos 4 caracteres'; render(); return; }
  if(state.isRegister && name.length<3){ state.loginError='Ingresa tu nombre completo'; render(); return; }
  if(state.isRegister && (age<1||age>120)){ state.loginError='Ingresa una edad válida (entre 1 y 120 años)'; render(); return; }

  state.busy=true; state.loginError=''; render();
  setTimeout(()=>{
    const derived = name || email.split('@')[0].replace(/[._]/g,' ').replace(/\b\w/g,c=>c.toUpperCase());
    DB.setUser({id:'user_01',fullName:derived,email,phone:'+34 600 123 456',age:state.isRegister?age:30});
    state.busy=false; state.screen='home'; state._scroll=0; render();
    toast('¡Bienvenido/a, '+derived.split(' ')[0]+'!');
  }, 900);
}

function startWizard(){ state.wStep=1; state.wDoctor=null; state.wSpecialty=null; state.wDate=null; state.wSlot=null; state.wReason=''; state.screen='wizard'; render(); }

function wNext(){
  if(state.wStep<3){ state.wStep++; render(); }
  else {
    const d=getDoctor(state.wDoctor);
    const appts=DB.loadAppointments();
    const overlaps=appts.some(a=>a.date===state.wDate&&a.time===state.wSlot&&a.status!=='Cancelled');
    if(overlaps){ toast('⚠️ Ya tienes una cita a esa hora'); return; }
    const user=DB.getUser();
    appts.push({id:'apt_'+Date.now(),doctorId:d.id,doctorName:displayName(d.fullName),specialtyName:d.spName,
      patientName:user.fullName,patientAge:user.age,date:state.wDate,time:state.wSlot,status:'Confirmed',
      reason:state.wReason,modality:state.wModality});
    DB.saveAppointments(appts);
    state.screen='appointments'; state._scroll=0; render();
    setTimeout(()=>toast('✅ Cita confirmada con '+displayName(d.fullName)),200);
  }
}

function ddConfirm(){
  const d=state.currentDoctor;
  const date=state.ddDate||fmtISO(selectableDates()[0]);
  const appts=DB.loadAppointments();
  const overlaps=appts.some(a=>a.date===date&&a.time===state.ddSlot&&a.status!=='Cancelled');
  if(overlaps){ toast('⚠️ Ya tienes una cita a esa hora'); return; }
  const user=DB.getUser();
  appts.push({id:'apt_'+Date.now(),doctorId:d.id,doctorName:displayName(d.fullName),specialtyName:d.spName,
    patientName:user.fullName,patientAge:user.age,date,time:state.ddSlot,status:'Confirmed',
    reason:state.ddReason,modality:'Presencial'});
  DB.saveAppointments(appts);
  state.screen='appointments'; state._scroll=0; render();
  setTimeout(()=>toast('✅ Cita confirmada con '+displayName(d.fullName)),200);
}

function cancelAppointment(id){
  if(!confirm('¿Cancelar esta cita?')) return;
  const appts=DB.loadAppointments();
  const a=appts.find(x=>x.id===id); if(a) a.status='Cancelled';
  DB.saveAppointments(appts);
  state.screen='appointments'; state._scroll=0; render();
  setTimeout(()=>toast('Cita cancelada'),200);
}

/* ---------- 10. ARRANQUE ---------- */
(function init(){
  DB.loadAppointments(); // sembrado inicial
  state.screen = DB.getUser() ? 'home' : 'login';
  render();
})();
