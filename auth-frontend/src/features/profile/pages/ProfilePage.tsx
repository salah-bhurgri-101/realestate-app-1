import { useEffect, useState } from "react";
import { getMeApi, logoutAllApi } from "../../../api/authApi";
import {
  addAddressApi,
  deleteAddressApi,
  getAddressesApi,
  getBlockedUsersApi,
  getFollowersApi,
  getFollowingApi,
  getFollowCountsApi,
  getUserProfileApi,
  getUserSettingsApi,
  followUserApi,
  blockUserApi,
  deleteUserProfileApi,
  updateAddressApi,
  unblockUserApi,
  updateUserProfileApi,
  updateUserSettingsApi,
  type Address,
  type UserProfile,
  type UserSettings,
} from "../../../api/userApi";
import { useAuthStore } from "../../auth/store/authStore";
import type { MeResponse } from "../../auth/types/auth.types";

const ProfilePage = () => {
  const { clearTokens } = useAuthStore();
  const [me, setMe] = useState<MeResponse | null>(null);
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [settings, setSettings] = useState<UserSettings | null>(null);
  const [addresses, setAddresses] = useState<Address[]>([]);
  const [blocked, setBlocked] = useState<string[]>([]);
  const [counts, setCounts] = useState({ followersCount: 0, followingCount: 0 });
  const [following, setFollowing] = useState<unknown[]>([]);
  const [followers, setFollowers] = useState<unknown[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [targetUserId, setTargetUserId] = useState("");

  useEffect(() => {
    const load = async () => {
      try {
        const response = await getMeApi();
        const data = response?.data || response;
        const id = data.user.id;
        const [profileData, settingsData, addressData, blockData, countData, followingData, followerData] =
          await Promise.all([
            getUserProfileApi(id),
            getUserSettingsApi(id),
            getAddressesApi(id),
            getBlockedUsersApi(id),
            getFollowCountsApi(id),
            getFollowingApi(id),
            getFollowersApi(id),
          ]);
        setMe(data);
        setProfile(profileData);
        setSettings(settingsData);
        setAddresses(addressData);
        setBlocked(blockData.content);
        setCounts(countData);
        setFollowing(followingData.content);
        setFollowers(followerData.content);
      } catch (reason) {
        setError(reason instanceof Error ? reason.message : "Unable to load user-service data.");
      } finally {
        setLoading(false);
      }
    };
    void load();
  }, []);

  const userId = me?.user.id;

  const saveProfile = async () => {
    if (!userId || !profile) return;
    try {
      setSaving(true);
      setProfile(await updateUserProfileApi(userId, profile));
      setMessage("Profile updated.");
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Unable to update profile.");
    } finally { setSaving(false); }
  };

  const saveSettings = async () => {
    if (!userId || !settings) return;
    try {
      setSaving(true);
      setSettings(await updateUserSettingsApi(userId, settings));
      setMessage("Settings updated.");
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Unable to update settings.");
    } finally { setSaving(false); }
  };

  const addAddress = async () => {
    if (!userId) return;
    try {
      const address = await addAddressApi(userId, { line1: "New address", city: "City", country: "US" });
      setAddresses((items) => [...items, address]);
      setMessage("Address added. Edit the values in the address manager.");
    } catch (reason) { setError(reason instanceof Error ? reason.message : "Unable to add address."); }
  };

  const deleteAddress = async (addressId: string) => {
    if (!userId) return;
    try { await deleteAddressApi(userId, addressId); setAddresses((items) => items.filter((item) => item.id !== addressId)); }
    catch (reason) { setError(reason instanceof Error ? reason.message : "Unable to delete address."); }
  };

  const editAddress = async (address: Address) => {
    if (!userId) return;
    const line1 = window.prompt("Address line 1", address.line1);
    if (!line1) return;
    try {
      const updated = await updateAddressApi(userId, address.id, {
        line1,
        line2: address.line2,
        city: address.city,
        state: address.state,
        postalCode: address.postalCode,
        country: address.country,
        label: address.label,
        isDefault: address.default,
      });
      setAddresses((items) => items.map((item) => item.id === updated.id ? updated : item));
    } catch (reason) { setError(reason instanceof Error ? reason.message : "Unable to update address."); }
  };

  const unblock = async (blockedId: string) => {
    if (!userId) return;
    try { await unblockUserApi(userId, blockedId); setBlocked((items) => items.filter((id) => id !== blockedId)); }
    catch (reason) { setError(reason instanceof Error ? reason.message : "Unable to unblock user."); }
  };

  const followOrBlock = async (action: "follow" | "block") => {
    if (!userId || !targetUserId) return;
    try {
      if (action === "follow") {
        await followUserApi(userId, targetUserId);
        setMessage("User followed.");
      } else {
        await blockUserApi(userId, targetUserId);
        setBlocked((items) => [...items, targetUserId]);
        setMessage("User blocked.");
      }
      setTargetUserId("");
    } catch (reason) { setError(reason instanceof Error ? reason.message : `Unable to ${action} user.`); }
  };

  const deleteProfile = async () => {
    if (!userId || !window.confirm("Delete your extended profile?")) return;
    try { await deleteUserProfileApi(userId); setProfile(null); setMessage("Extended profile deleted."); }
    catch (reason) { setError(reason instanceof Error ? reason.message : "Unable to delete profile."); }
  };

  if (loading) return <div className="p-8 text-slate-500">Loading profile, settings, addresses, follows, and blocks...</div>;
  if (!me || !profile || !settings) return <div className="p-8 text-rose-600">{error || "User data is unavailable."}</div>;

  return (
    <div className="mx-auto max-w-5xl space-y-6 p-6">
      <div><h1 className="text-3xl font-bold text-slate-950">My account</h1><p className="mt-2 text-slate-500">Manage all user-service data from one place.</p></div>
      {error && <p className="bg-rose-50 p-3 text-sm text-rose-700">{error}</p>}
      {message && <p className="bg-emerald-50 p-3 text-sm text-emerald-700">{message}</p>}
      <section className="rounded-2xl border bg-white p-6">
        <h2 className="text-xl font-bold">Profile</h2>
        <div className="mt-4 grid gap-4 md:grid-cols-2">
          <input className="rounded border p-3" value={profile.displayName ?? ""} onChange={(e) => setProfile({ ...profile, displayName: e.target.value })} placeholder="Display name" />
          <input className="rounded border p-3" value={profile.phone ?? ""} onChange={(e) => setProfile({ ...profile, phone: e.target.value })} placeholder="Phone" />
          <textarea className="rounded border p-3 md:col-span-2" value={profile.bio ?? ""} onChange={(e) => setProfile({ ...profile, bio: e.target.value })} placeholder="Bio" />
          <input className="rounded border p-3" value={profile.avatarUrl ?? ""} onChange={(e) => setProfile({ ...profile, avatarUrl: e.target.value })} placeholder="Avatar URL" />
          <input className="rounded border p-3" value={profile.coverUrl ?? ""} onChange={(e) => setProfile({ ...profile, coverUrl: e.target.value })} placeholder="Cover URL" />
        </div>
        <button onClick={() => void saveProfile()} disabled={saving} className="mt-4 rounded bg-slate-950 px-4 py-2 text-white">Save profile</button>
      </section>
      <section className="rounded-2xl border bg-white p-6">
        <h2 className="text-xl font-bold">Settings</h2>
        <div className="mt-4 grid gap-4 md:grid-cols-2">
          <input className="rounded border p-3" value={settings.language} onChange={(e) => setSettings({ ...settings, language: e.target.value })} placeholder="Language" />
          <input className="rounded border p-3" value={settings.timezone} onChange={(e) => setSettings({ ...settings, timezone: e.target.value })} placeholder="Timezone" />
          <label className="flex gap-2"><input type="checkbox" checked={settings.emailNotifications} onChange={(e) => setSettings({ ...settings, emailNotifications: e.target.checked })} /> Email notifications</label>
          <label className="flex gap-2"><input type="checkbox" checked={settings.pushNotifications} onChange={(e) => setSettings({ ...settings, pushNotifications: e.target.checked })} /> Push notifications</label>
          <label className="flex gap-2"><input type="checkbox" checked={settings.smsNotifications} onChange={(e) => setSettings({ ...settings, smsNotifications: e.target.checked })} /> SMS notifications</label>
        </div>
        <button onClick={() => void saveSettings()} disabled={saving} className="mt-4 rounded bg-slate-950 px-4 py-2 text-white">Save settings</button>
      </section>
      <section className="rounded-2xl border bg-white p-6">
        <div className="flex items-center justify-between"><h2 className="text-xl font-bold">Addresses</h2><button onClick={() => void addAddress()} className="rounded bg-slate-950 px-4 py-2 text-sm text-white">Add address</button></div>
        <div className="mt-4 space-y-2">{addresses.map((address) => <div key={address.id} className="flex justify-between rounded bg-slate-50 p-3"><span>{address.line1}, {address.city}, {address.country}</span><span className="flex gap-3"><button onClick={() => void editAddress(address)} className="text-sm text-slate-700">Edit</button><button onClick={() => void deleteAddress(address.id)} className="text-sm text-rose-600">Delete</button></span></div>)}</div>
      </section>
      <section className="grid gap-6 rounded-2xl border bg-white p-6 md:grid-cols-3">
        <div><p className="text-sm text-slate-500">Followers</p><p className="text-2xl font-bold">{counts.followersCount}</p><p className="text-xs text-slate-400">{followers.length} loaded</p></div>
        <div><p className="text-sm text-slate-500">Following</p><p className="text-2xl font-bold">{counts.followingCount}</p><p className="text-xs text-slate-400">{following.length} loaded</p></div>
        <div><p className="text-sm text-slate-500">Blocked users</p><p className="text-2xl font-bold">{blocked.length}</p>{blocked.map((id) => <button key={id} onClick={() => void unblock(id)} className="mr-2 text-xs text-rose-600">Unblock {id.slice(0, 8)}</button>)}</div>
      </section>
      <section className="rounded-2xl border bg-white p-6">
        <h2 className="text-xl font-bold">Social actions</h2>
        <div className="mt-4 flex flex-wrap gap-2">
          <input className="min-w-72 rounded border p-3" value={targetUserId} onChange={(e) => setTargetUserId(e.target.value)} placeholder="Target user UUID" />
          <button onClick={() => void followOrBlock("follow")} className="rounded bg-slate-950 px-4 py-2 text-white">Follow</button>
          <button onClick={() => void followOrBlock("block")} className="rounded bg-rose-600 px-4 py-2 text-white">Block</button>
        </div>
      </section>
      <button onClick={() => void deleteProfile()} className="rounded border border-rose-600 px-4 py-2 text-rose-600">Delete extended profile</button>
      <button onClick={() => void logoutAllApi().then(clearTokens)} className="rounded bg-rose-600 px-4 py-2 text-white">Logout all devices</button>
    </div>
  );
};

export default ProfilePage;
