// uploadDailyChallenges.js
// npm install firebase-admin
const admin = require('firebase-admin');

process.env.FIRESTORE_EMULATOR_HOST = process.env.FIRESTORE_EMULATOR_HOST || 'localhost:8080';
process.env.GCLOUD_PROJECT = process.env.GCLOUD_PROJECT || 'moco25-fernfreunde2'; // anpassen falls nötig

// Initialisierung für Emulator: ProjectId reicht
admin.initializeApp({ projectId: process.env.GCLOUD_PROJECT });

const db = admin.firestore();

function pad(n){ return n < 10 ? '0' + n : '' + n; }
function formatDateYMD(ts){
  const d = new Date(ts);
  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}`;
}

(async function main(){
  try{
    // Base: nächste volle Stunde (beginnt ab nächster Stunde)
    const now = Date.now();
    const nextHour = new Date(now);
    nextHour.setMinutes(0,0,0);
    const base = nextHour.getTime() + 3600*1000; // next hour start

    const collection = 'daily_challenges'; // <-- anpassen falls dein konstante anders heißt
    const types = ['ANY','PHOTO','VIDEO'];

    const batch = db.batch();

    for(let i=0;i<24;i++){
      const startAt = base + i * 3600*1000;
      const expiresAt = startAt + 3600*1000;
      const challengeId = `challenge-${new Date(startAt).toISOString().replace(/[:.]/g,'-')}`;
      const dateStr = formatDateYMD(startAt);
      const type = types[i % types.length];

      const docRef = db.collection(collection).doc(challengeId);

      const data = {
        challengeId: challengeId,
        date: dateStr,
        title: `Hourly Challenge #${i+1}`,
        text: `Challenge #${i+1} — startet ${new Date(startAt).toISOString()}`,
        challengeType: type,
        startAt: startAt,
        expiresAt: expiresAt,
        maxPostsPerUser: 1
      };

      batch.set(docRef, data);
      console.log('Queued', challengeId, 'startAt', new Date(startAt).toISOString());
    }

    await batch.commit();
    console.log('✓ Done — 24 challenges written to collection:', collection);
    process.exit(0);
  } catch (err) {
    console.error('ERROR', err);
    process.exit(1);
  }
})();
