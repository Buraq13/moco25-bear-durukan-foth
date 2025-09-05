// deleteGeneratedChallenges.js
// npm install firebase-admin
const admin = require('firebase-admin');

process.env.FIRESTORE_EMULATOR_HOST = process.env.FIRESTORE_EMULATOR_HOST || 'localhost:8080';
process.env.GCLOUD_PROJECT = process.env.GCLOUD_PROJECT || 'moco25-fernfreunde2';

admin.initializeApp({ projectId: process.env.GCLOUD_PROJECT });
const db = admin.firestore();

(async function cleanup(){
  try{
    const collection = 'daily_challenges';
    const snap = await db.collection(collection).get();
    const batch = db.batch();
    let deleted = 0;
    snap.forEach(doc => {
      if (doc.id.startsWith('challenge-')) {
        batch.delete(doc.ref);
        deleted++;
      }
    });
    if (deleted === 0) {
      console.log('No generated challenge docs found.');
      process.exit(0);
    }
    await batch.commit();
    console.log(`✓ Deleted ${deleted} documents from ${collection}`);
    process.exit(0);
  } catch (err) {
    console.error('ERROR', err);
    process.exit(1);
  }
})();
